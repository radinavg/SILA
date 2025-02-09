import numpy as np
import pandas as pd
from sklearn.metrics.pairwise import cosine_similarity

from flaskr.activity_attributes import ActivityAttributes
from flaskr.user_preferences import UserPreferences


class CosineSimilarityWrapper:

    def __init__(self, users_preferences: list[UserPreferences],
                 activities_attributes: list[ActivityAttributes]):
        self.users_preferences = {user_preferences.user_id: user_preferences for user_preferences in users_preferences}
        self.activities_attributes = {activity_attributes.activity_id: activity_attributes for activity_attributes in
                                      activities_attributes}

    def calculate_cosine_similarity_users(self):
        user_preference_list = [user.to_vector() for user in self.users_preferences.values()]

        return cosine_similarity(user_preference_list, user_preference_list)

    def calculate_cosine_similarity_activities(self):
        activity_attributes_list = [activity.to_vector() for activity in self.activities_attributes.values()]

        return cosine_similarity(activity_attributes_list, activity_attributes_list)


def calculate_cosine_similarity_matrix(matrix) -> list[list[float]]:
    if matrix.isnull().values.any():
        return cosine_similarity(matrix.fillna(0))
    return cosine_similarity(matrix)

def calculate_euclidean_distance_matrix(matrix) -> np.ndarray:
    if isinstance(matrix, pd.DataFrame):
        matrix = matrix.fillna(0).to_numpy()  # Convert DataFrame to NumPy array
    elif isinstance(matrix, np.ndarray):
        matrix = np.nan_to_num(matrix, nan=0.0)  # Replace NaNs if it's already an array

    return np.linalg.norm(matrix[:, np.newaxis] - matrix, axis=2)