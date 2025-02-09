import unittest
import pandas as pd
import xgboost as xgb

from flaskr.feature_importance import calculate_feature_importance_per_cluster


class TestFeatureImportance(unittest.TestCase):

    def setUp(self):
        # Create a sample dataset with features, labels, and cluster assignments
        data = {
            'feature1': [1, 2, 3, 4, 5, 6],
            'feature2': [6, 5, 4, 3, 2, 1],
            'label': [1, 0, 1, 0, 1, 0],
            'cluster': [0, 0, 1, 1, 2, 2]
        }
        self.df = pd.DataFrame(data)
        self.clusters = [0, 1, 2]

    def test_calculate_feature_importance_per_cluster(self):
        # Call the function with the sample data and clusters
        importance_df = calculate_feature_importance_per_cluster(self.df, self.clusters)

        # Check if the result is a DataFrame
        self.assertIsInstance(importance_df, pd.DataFrame)

        # Check if the necessary columns are present
        self.assertIn('Feature', importance_df.columns)
        self.assertIn('Importance', importance_df.columns)
        self.assertIn('Cluster', importance_df.columns)

        # Check if the importance values are reasonable (non-negative)
        self.assertTrue((importance_df['Importance'] >= 0).all())

        # Check if the result contains information for all clusters
        for cluster in self.clusters:
            self.assertTrue((importance_df['Cluster'] == cluster).any())

if __name__ == "__main__":
    unittest.main()
