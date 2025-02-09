import matplotlib
matplotlib.use('Agg')  # Use a non-GUI backend for saving images
import matplotlib.pyplot as plt
import seaborn as sns
import os
import numpy as np


def save_matrix_image(matrix, filename="heatmap.png", folder="static\images"):
    if not isinstance(matrix, np.ndarray) or matrix.ndim != 2:
        raise ValueError(f"Expected a 2D numpy array, but got shape={getattr(matrix, 'shape', None)}")

    os.makedirs(folder, exist_ok=True)

    plt.figure(figsize=(8, 6))
    sns.heatmap(matrix, annot=False, cmap="coolwarm")  # Customize annot=True to display values
    plt.title("Matrix Visualization")
    plt.xlabel("Columns")
    plt.ylabel("Rows")

    filepath = os.path.join(folder, filename)
    plt.savefig(filepath, bbox_inches="tight")
    plt.close()

    print(f"Saved to {filepath}")
    return filepath
