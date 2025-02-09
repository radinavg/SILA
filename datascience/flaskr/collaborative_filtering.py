import numpy as np
import pandas as pd
import logging
from flaskr.cosine_similarity_wrapper import calculate_cosine_similarity_matrix, calculate_euclidean_distance_matrix

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(name)s - %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S"
)
logger = logging.getLogger(__name__)


class CollaborativeFiltering:

    def __init__(self, df_ratings, df_users, df_studio_activities, df_activity_studio_mapping, df_visits, df_studios,
                 df_likes):
        self.df_ratings = df_ratings
        self.df_users = df_users
        self.df_studio_activities = df_studio_activities
        self.df_activity_studio_mapping = df_activity_studio_mapping
        self.df_visits = df_visits
        self.df_studios = df_studios
        self.df_likes = df_likes

        self.utility_matrix = self.make_utility_matrix()

    def baseline_prediction(self, user_id: int, studio_id: int):
        """Function to calculate baseline prediction from user and movie """

        data = self.utility_matrix

        # calculate global mean
        global_mean = data.stack().dropna().mean()

        # calculate user mean
        user_mean = data.loc[user_id, :].mean()

        # calculate studio mean
        studio_mean = data.loc[:, studio_id].mean()

        # calculate user bias
        user_bias = global_mean - user_mean

        # calculate item bias
        studio_bias = global_mean - studio_mean

        # calculate baseline
        baseline_ui = global_mean + user_bias + studio_bias

        return baseline_ui

    def recommend_items(self, user_id: int, num_recommendations: int = 10):
        similar_users_df = self.similar_users(user_id)

        data = self.utility_matrix

        # create empty dataframe to store prediction result
        prediction_df = pd.DataFrame()
        # create list to store prediction result
        predicted_ratings = []

        # items to predict are studios that the user has not rated
        items_to_predict = data.loc[user_id].index[data.loc[user_id].isna()]
        # items_to_predict = data.loc[user_id]
        logger.info(f"Items to predict for user {user_id}: {items_to_predict.values}")

        for studio_id in items_to_predict:
            prediction = self.predict_studio_rating(user_id=int(user_id), studio_id=int(studio_id),
                                               similar_users_df=similar_users_df, k=5)
            predicted_ratings.append(prediction)


        # logger.info(f"predicted_ratings:\n{predicted_ratings}")
        # assign studio_id
        prediction_df['studio_id'] = data.loc[user_id].index[data.loc[user_id].isna()]

        # assign prediction result
        prediction_df['predicted_ratings'] = predicted_ratings

        prediction_df = (prediction_df
                         .sort_values('predicted_ratings', ascending=False)
                         .head(num_recommendations))

        return prediction_df

    def predict_studio_rating(self, user_id: int, studio_id: int, similar_users_df, k):
        """Function to predict rating on user_id and studio_id"""

        # calculate baseline (u,i)
        baseline = self.baseline_prediction(user_id=user_id, studio_id=studio_id)

        # for sum
        sim_rating_total = 0
        similarity_sum = 0

        for i in range(k):
            neighbour_user_id = int(similar_users_df['application_user_id'][i])  # Convert to int

            if neighbour_user_id not in self.utility_matrix.index:
                logger.warning(f"neighbour_user_id {neighbour_user_id} not found in utility matrix index. Skipping...")
                continue

            neighbour_rating = self.utility_matrix.loc[neighbour_user_id, studio_id]

            if np.isnan(neighbour_rating):
                continue

            # calculate baseline (u,i)
            baseline = self.baseline_prediction(user_id=neighbour_user_id, studio_id=studio_id)

            # subtract baseline from rating
            adjusted_rating = neighbour_rating - baseline

            # multiply by similarity
            sim_rating = similar_users_df['similarity'][i] * adjusted_rating

            sim_rating_total += sim_rating
            similarity_sum += similar_users_df['similarity'][i]

        # avoiding ZeroDivisionError
        try:
            user_item_predicted_rating = baseline + (sim_rating_total / similarity_sum)

        except ZeroDivisionError:
            user_item_predicted_rating = baseline

        return user_item_predicted_rating

    def similar_users(self, user_id: int):
        utility_matrix = self.utility_matrix

        euclidean_distance_matrix = calculate_euclidean_distance_matrix(utility_matrix)

        euclidean_distance_df = pd.DataFrame(
            euclidean_distance_matrix,
            index=utility_matrix.index,
            columns=utility_matrix.index
        )
        logger.info(f"Euclidean distance:\n{euclidean_distance_df}")

        # num of similar users
        k = 10
        # Get top k similar users
        top_k_similar_users = euclidean_distance_df[user_id].sort_values(ascending=True)[:k]

        similar_users_df = top_k_similar_users.reset_index()
        similar_users_df.columns = ['application_user_id', 'similarity']
        logger.info(f"Similar users for user {user_id}:\n{similar_users_df}")

        return similar_users_df

    def make_utility_matrix(self):
        # explicitly convert indices and columns to integers
        self.df_users['application_user_id'] = self.df_users['application_user_id'].astype(int)
        self.df_studios['studio_id'] = self.df_studios['studio_id'].astype(int)

        utility_matrix = pd.DataFrame(index=self.df_users['application_user_id'], columns=self.df_studios['studio_id'])

        # studio ratings
        for _, row in self.df_ratings.iterrows():
            user_id = int(row['application_user_id'])
            studio_id = int(row['studio_id'])
            value = row['rating']
            if user_id in utility_matrix.index and studio_id in utility_matrix.columns and pd.notna(value):
                # subtract 3 from the rating to make it 0-based
                utility_matrix.loc[user_id, studio_id] = value - 3

        # liked studios
        for _, row in self.df_likes.iterrows():
            user_id = int(row['application_user_id'])
            studio_id = int(row['studio_id'])
            if user_id in utility_matrix.index and studio_id in utility_matrix.columns:
                utility_matrix.loc[user_id, studio_id] += 1

        # studio visits
        for _, row in self.df_visits.iterrows():
            user_id = int(row['application_user_id'])
            activity_id = int(row['studio_activity_id'])

            # Ensure activity_id exists in the mapping before accessing it
            studio_id_series = self.df_activity_studio_mapping.loc[
                self.df_activity_studio_mapping[
                    'studio_activities_studio_activity_id'] == activity_id, 'studio_studio_id'
            ]

            if not studio_id_series.empty:
                studio_id = int(studio_id_series.iloc[0])

                if user_id in utility_matrix.index and studio_id in utility_matrix.columns:
                    # Add 0.2 to final value for every visit
                    utility_matrix.loc[user_id, studio_id] += 0.2

        logger.info(f"Generated utility matrix:\n{utility_matrix}")
        return utility_matrix
