import unittest
import pandas as pd
import numpy as np
from scipy.spatial.distance import pdist, squareform
from sklearn.metrics.pairwise import cosine_similarity
from flaskr.geocoding import calculate_spatial_distances


class TestCalculateSpatialDistances(unittest.TestCase):

    def test_valid_coordinates(self):
        df = pd.DataFrame({
            "latitude": [48.2082, 48.3069, 48.8566],
            "longitude": [16.3738, 14.2858, 2.3522]
        })
        result = calculate_spatial_distances(df)
        self.assertEqual(result.shape, (3, 3))

    def test_missing_columns(self):
        df = pd.DataFrame({"lat": [48.2082, 48.3069], "lon": [16.3738, 14.2858]})
        with self.assertRaises(ValueError):
            calculate_spatial_distances(df)
            
    @unittest.skip("skip test")
    def test_nan_values(self):
        df = pd.DataFrame({
            "latitude": [48.2082, np.nan, 48.8566],
            "longitude": [16.3738, 14.2858, np.nan]
        })
        with self.assertRaises(ValueError):
            calculate_spatial_distances(df)

    def test_empty_dataframe(self):
        df = pd.DataFrame(columns=["latitude", "longitude"])
        with self.assertRaises(ValueError):
            calculate_spatial_distances(df)


if __name__ == '__main__':
    unittest.main()
