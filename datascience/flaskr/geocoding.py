import datetime

import numpy as np
import pandas as pd
from scipy.spatial.distance import pdist, squareform
from sklearn.metrics.pairwise import cosine_similarity


def calculate_spatial_distances(df: pd.DataFrame) -> np.ndarray:
    """
    Calculate a spatial distance matrix using geodesic distances.
    The DataFrame must contain 'latitude' and 'longitude' columns.
    Returns normalized matrix of spatial distances.
    """
    if "latitude" not in df.columns or "longitude" not in df.columns:
        raise ValueError("The DataFrame must contain 'latitude' and 'longitude' columns.")

    # Filter out rows with NaN coordinates
    df = df.dropna(subset=["latitude", "longitude"])

    if df.empty:
        raise ValueError("No valid coordinates available for distance calculation.")

    coords = df[["latitude", "longitude"]].values

    spatial_distances = squareform(pdist(coords, metric="euclidean"))
    return cosine_similarity(spatial_distances)


class Geocoding:
    def __init__(self):
        pass
