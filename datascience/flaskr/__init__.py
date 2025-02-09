import os
import logging
import pandas as pd
from flask import Flask, jsonify, Response
from flask_sqlalchemy import SQLAlchemy
from sqlalchemy import create_engine, text

from flaskr.activity_attributes import ActivityAttributes
from flaskr.clustering import Clustering
from flaskr.collaborative_filtering import CollaborativeFiltering
from flaskr.plotting import save_matrix_image
from flaskr.time_decay_calculation import calculate_time_decay
from flaskr.geocoding import Geocoding
from flaskr.user_preferences import UserPreferences

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(name)s - %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S"
)
logger = logging.getLogger(__name__)

db = SQLAlchemy()


def get_db_connection():
    engine = create_engine("postgresql://admin:admin@postgres-db:5432/sila-db")
    return engine


def create_app(test_config=None):
    app = Flask(__name__, instance_relative_config=True)

    # Load default configuration
    app.config.from_mapping(
        SECRET_KEY=os.getenv("SECRET_KEY", "dev"),
        SQLALCHEMY_DATABASE_URI=os.getenv(
            "DATABASE_URL", "postgresql://admin:admin@postgres-db:5432/sila-db"
        ),
        SQLALCHEMY_TRACK_MODIFICATIONS=False,
    )

    if test_config is not None:
        app.config.update(test_config)

    db.init_app(app)

    try:
        os.makedirs(app.instance_path)
    except OSError:
        pass

    def fetch_data(engine):

        # Fetch explicit ratings (studio reviews)
        query_ratings = """
        SELECT r.application_user_id, r.studio_id, r.rating
        FROM review r;
        """
        df_ratings = pd.read_sql(query_ratings, engine)

        # Fetch implicit feedback (user visit history)
        query_visits = """
        SELECT usa.* FROM user_studio_activities usa
        JOIN application_user au ON usa.application_user_id = au.application_user_id
        AND au.is_admin = FALSE AND au.is_studio_admin = FALSE AND au.is_locked = FALSE;
        """
        df_visits = pd.read_sql(query_visits, engine)

        # Fetch implicit feedback (user visit history)
        query_likes = """
        SELECT *
        FROM favourite_studios;
        """
        df_likes = pd.read_sql(query_likes, engine)

        # Fetch studio activities
        query_studio_activities = """
        SELECT *
        FROM studio_activity sa 
        WHERE sa.date_time > CURRENT_TIMESTAMP;
        """
        df_studio_activities = pd.read_sql(query_studio_activities, engine)

        # Fetch studios
        query_studios = """
        SELECT *
        FROM studio;
        """
        df_studios = pd.read_sql(query_studios, engine)

        query_activity_studio = """
        SELECT ssa.studio_studio_id,
               ssa.studio_activities_studio_activity_id
        FROM studio_studio_activities ssa;
        """
        df_activity_studio_mapping = pd.read_sql(query_activity_studio, engine)

        # Fetch user data
        query_users = """
        SELECT au.application_user_id, au.location, au.longitude, au.latitude
        FROM application_user au, application_user_preferences aup
        WHERE au.application_user_id = aup.application_user_id
            AND au.is_admin = FALSE AND au.is_studio_admin = FALSE
            AND au.is_locked = FALSE
            AND au.longitude <> 0 AND au.latitude <> 0;
        """
        df_users = pd.read_sql(query_users, engine)

        # Fetch recommendation cluster data
        query_clusters = """
        SELECT * FROM recommendation_cluster;
        """
        df_recommendation_clusters = pd.read_sql(query_clusters, engine)

        # Fetch user preferences
        query_user_preferences_for_cluster = """
        SELECT aup.* FROM application_user_preferences aup
        JOIN application_user au ON aup.application_user_id = au.application_user_id
        WHERE au.is_admin = FALSE AND au.is_studio_admin = FALSE AND au.is_locked = FALSE;
        """
        df_user_preferences = pd.read_sql(query_user_preferences_for_cluster, engine)

        # Fetch activity attributes
        query_activity_attributes = """
        SELECT sap.*
        FROM studio_activity_preferences sap
        JOIN studio_activity sa ON sap.id = sa.preferences_id
        LEFT JOIN user_studio_activities usa ON sa.studio_activity_id = usa.studio_activity_id
        GROUP BY sap.id, sa.capacity, sa.date_time
        HAVING COUNT(usa.application_user_id) < sa.capacity
        AND sa.date_time > CURRENT_TIMESTAMP;
        """
        df_activity_attributes = pd.read_sql(query_activity_attributes, engine)

        return df_ratings, df_visits, df_likes, df_studio_activities, df_studios, df_activity_studio_mapping, df_users, df_recommendation_clusters, df_user_preferences, df_activity_attributes

    @app.route('/collaborative-filtering/<user_id>')
    def collaborative_filter(user_id):
        logger.info("Connecting to database using SQLAlchemy...")
        engine = get_db_connection()

        df_ratings, df_visits, df_likes, df_studio_activities, df_studios, df_activity_studio_mapping, df_users, \
            df_recommendation_clusters, df_user_preferences, df_activity_attributes = fetch_data(engine)

        ################################
        # Make Utility Matrix
        ################################
        collab = CollaborativeFiltering(df_ratings, df_users, df_studio_activities, df_activity_studio_mapping,
                                        df_visits, df_studios, df_likes)

        if not user_id.isdigit() or int(user_id) < 0:
            return Response("user_id has to be a positive integer!", status=400, mimetype='application/json')

        recommended_activities = collab.recommend_items(int(user_id), 10)
        logger.info(f"Recommended studios for user {user_id}: {recommended_activities}")

        ##############################################
        # Save Studio recommendations to the data base
        ##############################################
        with engine.begin() as conn:
            delete_query = text("""
                DELETE FROM user_studio_recommendations
                WHERE application_user_id = :user_id
            """)
            conn.execute(delete_query, {"user_id": int(user_id)})

            insert_query = text("""
                INSERT INTO user_studio_recommendations (application_user_id, studio_id)
                VALUES (:user_id, :studio_id)
            """)

            for studio_id in recommended_activities["studio_id"]:
                conn.execute(insert_query, {"user_id": int(user_id), "studio_id": studio_id})

        return Response("Collaborative filtering successful!", status=200, mimetype='application/json')

    @app.route('/on-preference')
    def on_preference():
        logger.info("Connecting to database using SQLAlchemy...")
        engine = get_db_connection()

        df_ratings, df_visits, df_likes, df_studio_activities, df_studios, df_activity_studio_mapping, df_users, df_recommendation_clusters, df_user_preferences, df_activity_attributes = fetch_data(
            engine)

        user_preferences_list_for_cluster = []
        for _, row in df_user_preferences.iterrows():
            pref = UserPreferences(row)
            user_preferences_list_for_cluster.append(pref)

        activity_attributes_list = []
        for _, row in df_activity_attributes.iterrows():
            pref = ActivityAttributes(row)
            activity_attributes_list.append(pref)

        ################################
        # Perform Clustering
        ################################

        pam_clustering = Clustering(activityAttributes=activity_attributes_list,
                                    userPreferences=user_preferences_list_for_cluster,
                                    df_users=df_users)

        user_clusters = pam_clustering.pam_clustering_with_elbow_method(alpha=0.8)
        cluster_labels = user_clusters["cluster_labels"].tolist()

        ################################
        # Update user and cluster in the database based on the clustering results
        ################################

        existing_cluster_ids = set(df_recommendation_clusters["recommendation_cluster_id"].tolist())

        new_cluster_ids = set(cluster_labels) - existing_cluster_ids

        if new_cluster_ids:
            logger.info(f"Inserting new recommendation_cluster instances: {new_cluster_ids}")
            with engine.begin() as conn:
                conn.execute(
                    text("INSERT INTO recommendation_cluster (recommendation_cluster_id) VALUES (:cluster_id)"),
                    [{"cluster_id": cluster_id} for cluster_id in new_cluster_ids]
                )

        update_data = [
            {"cluster_id": cluster_labels[i], "user_id": row["application_user_id"]}
            for i, row in df_users.iterrows()
        ]

        with engine.begin() as conn:
            conn.execute(
                text("UPDATE application_user "
                     "SET recommendation_cluster_id = :cluster_id "
                     "WHERE application_user_id = :user_id"),
                update_data
            )

        ################################
        # Delete existing cluster-activity mappings in the DB
        ################################

        query_recommendation_cluster_recommended_activities = """
            SELECT * FROM recommendation_cluster_recommended_activities;
            """
        df_recommendation_cluster_recommended_activities = pd.read_sql(
            query_recommendation_cluster_recommended_activities, engine)
        recommendation_cluster_recommendation_cluster_ids = df_recommendation_cluster_recommended_activities[
            "recommendation_cluster_recommendation_cluster_id"].tolist()

        if recommendation_cluster_recommendation_cluster_ids:
            logger.info("Delete current clusters...")
            # if clustering results already are saved, drop table and insert them again
            with engine.begin() as conn:
                conn.execute(
                    text(
                        "DELETE FROM recommendation_cluster_recommended_activities;")
                )

        ################################
        # Calculate similarity between preferences of user clusters and activity attributes
        ################################

        query_user_preferences_with_cluster_id = f"""
        SELECT aup.*, au.recommendation_cluster_id FROM application_user_preferences aup, application_user au
        WHERE au.recommendation_cluster_id IS NOT NULL AND aup.application_user_id = au.application_user_id;
        """
        df_user_preferences_with_cluster_id = pd.read_sql(query_user_preferences_with_cluster_id, engine)

        recommendations_to_insert = []
        for label in set(cluster_labels):
            logger.info(f"Calculating similarity for cluster {label}...")

            # Fetch user preferences for each cluster with pandas
            df_cleaned = df_user_preferences_with_cluster_id.loc[
                df_user_preferences_with_cluster_id['recommendation_cluster_id'] == label]

            user_preferences_list_for_cluster = []
            for _, row in df_cleaned.iterrows():
                pref = UserPreferences(row)
                user_preferences_list_for_cluster.append(pref)

            recommended_activities = pam_clustering.user_cluster_similarity_with_activity_attributes(
                user_preferences_for_cluster=user_preferences_list_for_cluster
            )

            for activity_id in recommended_activities.keys():
                recommendations_to_insert.append({
                    "label": label,
                    "activity_id": activity_id
                })

        ################################
        # Insert recommendations to the recommendation_cluster_recommended_activities table
        ################################

        logger.info("Inserting Recommendations...")
        if recommendations_to_insert:
            with engine.begin() as conn:
                # Batch insert recommendations
                conn.execute(
                    text("""
                    INSERT INTO recommendation_cluster_recommended_activities 
                    (recommendation_cluster_recommendation_cluster_id, recommended_activities_studio_activity_id) 
                    VALUES (:label, :activity_id)
                    """),
                    recommendations_to_insert
                )

        return jsonify({
            "recommendations_created_successfully": True
        })

    return app
