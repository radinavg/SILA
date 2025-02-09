import unittest
import numpy as np
import pandas as pd
from flaskr.activity_attributes import ActivityAttributes
from flaskr.user_preferences import UserPreferences
from flaskr.clustering import Clustering, find_number_of_optimal_clusters
from flaskr.cosine_similarity_wrapper import CosineSimilarityWrapper
from flaskr.geocoding import calculate_spatial_distances


class TestClustering(unittest.TestCase):

    def setUp(self):
        # Sample user preferences data
        user_data = [
            {"application_user_id": 1, "prefers_individual": 1, "prefers_team": 0, "prefers_water_based": 0,
             "prefers_indoor": 1, "prefers_outdoor": 0, "prefers_both_indoor_and_outdoor": 0,
             "prefers_warm_climate": 1, "prefers_cold_climate": 0, "rain_compatibility": 0, "wind_suitability": 1,
             "focus_upper_body": 1, "focus_lower_body": 0, "focus_core": 1, "focus_full_body": 0,
             "is_beginner": 1, "is_intermediate": 0, "is_advanced": 0, "physical_demand_level": 5,
             "goal_strength": 1, "goal_endurance": 0, "goal_flexibility": 0, "goal_balance_coordination": 0,
             "goal_mental_focus": 1},
            {"application_user_id": 2, "prefers_individual": 0, "prefers_team": 1, "prefers_water_based": 1,
             "prefers_indoor": 0, "prefers_outdoor": 1, "prefers_both_indoor_and_outdoor": 0,
             "prefers_warm_climate": 0, "prefers_cold_climate": 1, "rain_compatibility": 1, "wind_suitability": 0,
             "focus_upper_body": 0, "focus_lower_body": 1, "focus_core": 0, "focus_full_body": 1,
             "is_beginner": 0, "is_intermediate": 1, "is_advanced": 0, "physical_demand_level": 7,
             "goal_strength": 0, "goal_endurance": 1, "goal_flexibility": 1, "goal_balance_coordination": 1,
             "goal_mental_focus": 0}
        ]

        # Sample activity attributes data
        activity_data = [
            {"id": 101, "is_individual": 1, "is_team": 0, "is_water_based": 0,
             "is_indoor": 1, "is_outdoor": 0, "is_both_indoor_and_outdoor": 0,
             "suitable_warm_climate": 1, "suitable_cold_climate": 0, "rain_compatibility": 0, "wind_suitability": 1,
             "involves_upper_body": 1, "involves_lower_body": 0, "involves_core": 1, "involves_full_body": 0,
             "is_beginner": 1, "is_intermediate": 0, "is_advanced": 0, "physical_demand_level": 5,
             "goal_strength": 1, "goal_endurance": 0, "goal_flexibility": 0, "goal_balance_coordination": 0,
             "goal_mental_focus": 1},
            {"id": 102, "is_individual": 0, "is_team": 1, "is_water_based": 1,
             "is_indoor": 0, "is_outdoor": 1, "is_both_indoor_and_outdoor": 0,
             "suitable_warm_climate": 0, "suitable_cold_climate": 1, "rain_compatibility": 1, "wind_suitability": 0,
             "involves_upper_body": 0, "involves_lower_body": 1, "involves_core": 0, "involves_full_body": 1,
             "is_beginner": 0, "is_intermediate": 1, "is_advanced": 0, "physical_demand_level": 7,
             "goal_strength": 0, "goal_endurance": 1, "goal_flexibility": 1, "goal_balance_coordination": 1,
             "goal_mental_focus": 0}
        ]

        self.users = [UserPreferences(pd.Series(user)) for user in user_data]
        self.activities = [ActivityAttributes(pd.Series(activity)) for activity in activity_data]
        self.wrapper = CosineSimilarityWrapper(self.users, self.activities)

        # Create a DataFrame for users with latitude and longitude for spatial distances
        self.df_users = pd.DataFrame({
            'user_id': [1, 2],
            'latitude': [51.5074, 48.8566],
            'longitude': [-0.1278, 2.3522]
        })

        # Initialize the Clustering class
        self.clustering = Clustering(activityAttributes=self.activities,
                                     userPreferences=self.users,
                                     df_users=self.df_users)

    def test_combine_distances_with_alpha(self):
        # Test with alpha = 0.5 (equal weight for cosine similarity and spatial distances)
        combined_distances_50 = self.clustering.combine_distances(alpha=0.5)
        self.assertEqual(combined_distances_50.shape, (2, 2))

        # Test with alpha = 1.0 (only cosine similarity)
        combined_distances_100 = self.clustering.combine_distances(alpha=1.0)
        self.assertEqual(combined_distances_100.shape, (2, 2))

        # Test with alpha = 0.0 (only spatial distances)
        combined_distances_0 = self.clustering.combine_distances(alpha=0.0)
        self.assertEqual(combined_distances_0.shape, (2, 2))

    def test_pam_clustering_with_elbow_method(self):
        # Test the PAM clustering with elbow method
        results = self.clustering.pam_clustering_with_elbow_method(alpha=0.8)

        # Check if the results contain optimal clusters and cluster labels
        self.assertIn("optimal_clusters", results)
        self.assertIn("cluster_labels", results)

        # Ensure that the number of cluster labels matches the number of users
        self.assertEqual(len(results["cluster_labels"]), len(self.df_users))

        # Ensure that the optimal number of clusters is reasonable
        self.assertGreater(results["optimal_clusters"], 0)
        self.assertLessEqual(results["optimal_clusters"], len(self.df_users))

    def test_user_cluster_similarity_with_activity_attributes(self):
        # Get user preferences for the first cluster (for testing purposes)
        user_preferences_for_cluster = self.users

        # Calculate the user cluster similarity with activity attributes
        similarity = self.clustering.user_cluster_similarity_with_activity_attributes(user_preferences_for_cluster)

        # Ensure that the result is a dictionary
        self.assertIsInstance(similarity, dict)

        # Ensure that the dictionary contains activity IDs as keys
        self.assertIn(101, similarity)
        self.assertIn(102, similarity)

    def test_find_number_of_optimal_clusters(self):
        # Test with a combined distance matrix with fewer than 2 samples
        small_combined_distances = np.array([[0]])  # 1x1 matrix
        with self.assertRaises(ValueError):
            find_number_of_optimal_clusters(small_combined_distances)

    def test_find_number_of_optimal_clusters_positive(self):
        # Test with a combined distance matrix with more than 1 sample
        combined_distances = np.array([[0, 1, 2], [1, 0, 1.5], [2, 1.5, 0]])  # 3x3 matrix
        optimal_k = find_number_of_optimal_clusters(combined_distances)

        # Check if the returned optimal_k is an integer and greater than or equal to 1
        self.assertIsInstance(optimal_k, int)
        self.assertGreaterEqual(optimal_k, 1)


if __name__ == "__main__":
    unittest.main()
