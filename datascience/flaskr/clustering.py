import kmedoids
import numpy as np
from pandas import DataFrame
from sklearn.cluster import KMeans
import logging
from flaskr.activity_attributes import ActivityAttributes
from flaskr.cosine_similarity_wrapper import CosineSimilarityWrapper
from flaskr.geocoding import calculate_spatial_distances
from flaskr.user_preferences import UserPreferences

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(name)s - %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S"
)
logger = logging.getLogger(__name__)


def find_number_of_optimal_clusters(combined_distances, k_max: int = 20):
    """
    Use the Elbow Method with KMeans to determine the optimal number of clusters.
    """
    if combined_distances.shape[0] < 2:
        raise ValueError("The dataset must have at least 2 samples to perform clustering.")

    distortions = []
    for k in range(1, min(k_max + 1, combined_distances.shape[0])):
        kmeans = KMeans(n_clusters=k, random_state=42)
        kmeans.fit(combined_distances)
        distortions.append(kmeans.inertia_)

    # Find the "elbow" point
    optimal_k = 1
    for i in range(1, len(distortions) - 1):
        if distortions[i - 1] - distortions[i] < distortions[i] - distortions[i + 1]:
            optimal_k = i + 1
            break

    logger.info(f"Optimal number of clusters determined by Elbow Method: {optimal_k}")
    return optimal_k


class Clustering:

    def __init__(self, activityAttributes: list[ActivityAttributes], userPreferences: list[UserPreferences],
                 df_users: DataFrame):
        self.activityAttributes = activityAttributes
        self.userPreferences = userPreferences
        self.df_users = df_users
        self.cluster_labels = []

    def combine_distances(self, alpha: float = 0.8):
        spatial_distances = np.ndarray
        try:
            spatial_distances = calculate_spatial_distances(self.df_users)
        except ValueError as e:
            logger.error(f"Error calculating spatial distances: {e}")

        # plot
        # save_matrix_image(spatial_distances, filename="spatial_distances.png")

        filtering = CosineSimilarityWrapper(users_preferences=self.userPreferences,
                                            activities_attributes=self.activityAttributes)
        cosine_sim = filtering.calculate_cosine_similarity_users()

        combined = alpha * cosine_sim + (1 - alpha) * spatial_distances
        return combined

    def user_cluster_similarity_with_activity_attributes(self,
                                                         user_preferences_for_cluster: list[UserPreferences]) -> dict:
        """
        Similarity function between cluster preferences (means) and activity attributes.
        """

        user_preferences_list_for_cluster = [user.to_vector() for user in user_preferences_for_cluster]
        activity_attributes_list = [activity.to_vector() for activity in self.activityAttributes]

        avg_list = [sum(x) / len(user_preferences_list_for_cluster) for x in zip(*user_preferences_list_for_cluster)]

        euclidean_distance_list = list()
        for attributes in activity_attributes_list:
            euclidean_dist = np.linalg.norm(np.array(avg_list) - np.array(attributes))
            euclidean_distance_list.append(euclidean_dist)

        activity_id_list = [activity.activity_id for activity in self.activityAttributes]
        euclidean_distance_dict = dict(map(lambda x, y: (x, y), activity_id_list, euclidean_distance_list))

        sorted_dict = dict(sorted(euclidean_distance_dict.items(), key=lambda item: item[1], reverse=True))

        sorted_dict = {key: sorted_dict[key] for key in list(sorted_dict)[:5]}

        logger.info(f"Top 5 activity recommendations based on cluster similarity: {sorted_dict}")

        return sorted_dict

    def pam_clustering_with_elbow_method(self, alpha: float = 0.8):
        """
        Perform PAM clustering using combined distance matrices and find optimal clusters using the Elbow Method.
        """
        # Combine distance matrices
        combined_distances = self.combine_distances(alpha=alpha)

        optimal_clusters = find_number_of_optimal_clusters(combined_distances=combined_distances)

        # Perform PAM clustering with the optimal number of clusters
        pam = kmedoids.KMedoids(n_clusters=optimal_clusters, metric='euclidean', random_state=123)
        self.cluster_labels = pam.fit_predict(combined_distances)

        results = {
            "optimal_clusters": optimal_clusters,
            "cluster_labels": self.cluster_labels
        }

        logger.info(f"PAM clustering completed with {optimal_clusters} clusters.")
        return results
