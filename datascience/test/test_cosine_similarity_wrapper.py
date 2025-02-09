import unittest
import pandas as pd
import numpy as np
from flaskr.activity_attributes import ActivityAttributes
from flaskr.user_preferences import UserPreferences
from flaskr.cosine_similarity_wrapper import CosineSimilarityWrapper, calculate_cosine_similarity_matrix


class TestCosineSimilarityWrapper(unittest.TestCase):

    def setUp(self):
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

    def test_calculate_cosine_similarity_users(self):
        similarity_matrix = self.wrapper.calculate_cosine_similarity_users()
        self.assertEqual(similarity_matrix.shape, (2, 2))
        self.assertTrue(np.allclose(np.diag(similarity_matrix), 1))  # Diagonal should be 1

    def test_calculate_cosine_similarity_activities(self):
        similarity_matrix = self.wrapper.calculate_cosine_similarity_activities()
        self.assertEqual(similarity_matrix.shape, (2, 2))
        self.assertTrue(np.allclose(np.diag(similarity_matrix), 1))  # Diagonal should be 1

    def test_calculate_cosine_similarity_matrix(self):
        matrix = pd.DataFrame([[1, 0, 1], [0, 1, 0], [1, 1, 1]])
        similarity_matrix = calculate_cosine_similarity_matrix(matrix)
        self.assertEqual(similarity_matrix.shape, (3, 3))
        self.assertTrue(np.allclose(np.diag(similarity_matrix), 1))  # Diagonal should be 1

    def test_calculate_cosine_similarity_matrix_with_nan(self):
        matrix = pd.DataFrame([[1, np.nan, 1], [0, 1, np.nan], [1, 1, 1]])
        similarity_matrix = calculate_cosine_similarity_matrix(matrix)
        self.assertEqual(similarity_matrix.shape, (3, 3))
        self.assertTrue(np.allclose(np.diag(similarity_matrix), 1))  # Diagonal should be 1

    def test_user_activity_similarity(self):
        user_similarity = self.wrapper.calculate_cosine_similarity_users()
        activity_similarity = self.wrapper.calculate_cosine_similarity_activities()

        # Check if the user and activity similarity matrices have the correct shape
        self.assertEqual(user_similarity.shape, (2, 2))
        self.assertEqual(activity_similarity.shape, (2, 2))

        # Ensure the diagonal of both similarity matrices is 1 (self-similarity)
        self.assertTrue(np.allclose(np.diag(user_similarity), 1))
        self.assertTrue(np.allclose(np.diag(activity_similarity), 1))

        # Check for some non-diagonal elements for user similarity
        self.assertTrue(np.any(user_similarity[0, 1] != 1))

        # Check for some non-diagonal elements for activity similarity
        self.assertTrue(np.any(activity_similarity[0, 1] != 1))


if __name__ == '__main__':
    unittest.main()
