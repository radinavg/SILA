### **Sprint 1 (08.01-16-01): Foundation and Setup**
#### **Nikola: Data Preparation**
- Implement geocoding and spatial distance calculation for user locations (`geocode_address` and `calculate_spatial_distances` functions).
- Set up the database schema for user preferences and activity features, ensuring proper relationships between tables (`UserPreferences`, `ApplicationUser`, etc.).

#### **Stiki: Preference Distance Calculation**
- Implement preference distance calculation, including normalizing data and handling various user preference attributes (`calculate_preference_distances` and `normalize` functions).
- Prepare synthetic user preferences and activity datasets for testing the functions.

#### **Maja: Clustering**
- Implement spatial and preference distance matrix combination logic (`combine_distances`).
- Start setting up PAM clustering, including testing with gap statistics for optimal clusters (`pam_clustering_with_gap_statistic`).

#### **Radina: Recommender Model Design**
- Design and implement content-based filtering for recommending activities based on user preferences and activity features.
- Draft the initial plan for regression-based suitability scoring (e.g., synthetic score generation, training, and evaluation).

#### **Ivan: Testing and Evaluation Framework**
- Develop a framework for testing reproducibility (mock data creation, idempotency checks, and user feedback simulation).
- Prepare initial tests for distance matrices and clustering results.

---

### **Sprint 2 (17.01-22.01): Core Development**
#### **Person 1: Activity Filtering**
- Implement activity filtering by proximity to cluster centers (`filter_activities_by_distance`).
- Calculate cluster centers based on user locations (`calculate_cluster_center`).

#### **Person 2: Clustering Refinement**
- Finalize PAM clustering with combined distances and test with varying alpha values.
- Visualize clustering results and ensure clusters make sense spatially and preference-wise (`plot_clusters`).

#### **Person 3: Machine Learning Models**
- Train and evaluate regression models (Linear Regression, Random Forest, XGBoost) for predicting activity suitability scores.
- Compare model performance using RMSE and R-squared metrics.

#### **Person 4: Integration and Storage**
- Integrate clustering results with the database (`write_clusters_to_db`).
- Add new fields to `ApplicationUser` for storing cluster IDs and update the database schema accordingly.

#### **Person 5: Recommender Validation**
- Test the recommender pipeline using controlled datasets, ensuring proper recommendations for each cluster.
- Simulate various user feedback scenarios and validate recommendations.

---

### **Sprint 3 (23.01-28-01): Optimization and Deployment**
#### **Person 1: Optimization**
- Optimize geocoding and spatial distance calculations for scalability.
- Implement caching or batch processing for repeated geocoding tasks.

#### **Person 2: Clustering Optimization**
- Experiment with different clustering algorithms (DBSCAN, K-Means) and compare results with PAM.
- Evaluate the impact of varying alpha values on clustering quality.

#### **Person 3: Model Refinement**
- Fine-tune regression models with hyperparameter optimization.
- Incorporate additional features or weighting mechanisms (e.g., time decay) into the recommender system.

#### **Person 4: End-to-End Integration**
- Integrate all modules into an end-to-end pipeline for clustering and recommending activities.
- Prepare scripts to automate the workflow (e.g., clustering, storing results, and generating recommendations).

#### **Person 5: Final Testing and Documentation**
- Perform idempotency and reproducibility testing on the entire system.
- Document the implementation process, challenges, and solutions.

---

### Deliverables for Each Week
- **Week 1:** Functional modules for distance calculations, preference extraction, clustering setup, and testing framework.
- **Week 2:** Working clustering and recommendation models integrated with the database.
- **Week 3:** Optimized, fully integrated, and tested recommender system ready for deployment.
