## Algorithm Approach

### User Clustering based on Preferences

#### General Approach

1. We calculate spatial distances between users based on their locations (people who live close to each other might like to go to same activities that aren't spatially distant to them)
2. Compute Preference Distances from user preferences.
3. Combine the Two Distance Matrices with a weighted parameter (α) - this parameter tells us what is more important, spatial closeness or preferences.
4. Cluster Users using the combined distance matrix.
5. Generate Recommendations for each cluster.

#### Needed packages
- `pandas`: Data processing
- `geopy`: Geocoding and spatial distances
- `scipy`: Distance matrices
- `scikit-learn-extra`: Clustering
- `gap-statistic`: Get right number of clusters
- `pyclustering`
- `matplotlib`
- `seaborn`

TODO: Add these dependencies to `requirements.txt`.

**Note:** This approach is based on my bachelor thesis, I didn't try the code out, it needs many improvements and so on, but I just wanted to show general idea of how this could be done.

#### Preparation & User Clustering

We prepare data for clustering:

1. We calculate spatial distances of user, don't cluster users that live far away from each other together:

We need to get longitude and latitude of our textual addresses:

```python
def geocode_address(address):
    """Geocode a single address to latitude and longitude."""
    geolocator = Nominatim(user_agent="spatial-clustering-service")
    location = geolocator.geocode(address)
    if location:
        return (location.latitude, location.longitude)
    else:
        return (None, None)
```

When having longitude and latitude of each user location we can calculate the spatial distance matrix:

```python
def calculate_spatial_distances(users):
    """Calculate spatial distance matrix using geodesic distances."""
    geolocator = Nominatim(user_agent="spatial-clustering-service")
    coords = []

    # Geocode all user addresses
    for user in users:
        address = user["location"]
        latitude, longitude = geocode_address(address)
        if latitude and longitude:
            coords.append((latitude, longitude))
        else:
            raise ValueError(f"Could not geocode address: {address}")

    # Calculate pairwise geodesic distances
    spatial_distances = np.array([
        [geodesic(coords[i], coords[j]).kilometers for j in range(len(coords))]
        for i in range(len(coords))
    ])
    return spatial_distances
```

2. We have to take a look at user preferences and calculate distances of types of activities users prefer. Now it's kinda complicated to think properly about this because of the way how our user preference data is saved. I would create a separate table that captures user and their preferences, something like:

```java
@Entity
@Table(name = "user_preferences")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_user_id", nullable = false, unique = true)
    private ApplicationUser user;

    @Column(name = "prefers_individual")
    private boolean prefersIndividual;

    @Column(name = "prefers_team")
    private boolean prefersTeam;

    @Column(name = "prefers_water_based")
    private boolean prefersWaterBased;

    @Column(name = "activity_setting")
    private String activitySetting; // "Indoor", "Outdoor", "Both"

    @Column(name = "prefers_warm_climate")
    private boolean prefersWarmClimate;

    @Column(name = "prefers_cold_climate")
    private boolean prefersColdClimate;

    @Column(name = "rain_compatibility")
    private boolean rainCompatibility;

    @Column(name = "wind_suitability")
    private boolean windSuitability;

    @Column(name = "focus_upper_body")
    private boolean focusUpperBody;

    @Column(name = "focus_lower_body")
    private boolean focusLowerBody;

    @Column(name = "focus_core")
    private boolean focusCore;

    @Column(name = "focus_full_body")
    private boolean focusFullBody;

    @Column(name = "is_beginner")
    private boolean isBeginner;

    @Column(name = "is_intermediate")
    private boolean isIntermediate;

    @Column(name = "is_advanced")
    private boolean isAdvanced;

    @Column(name = "prefers_minimal_equipment")
    private boolean prefersMinimalEquipment;

    @Column(name = "has_specialized_equipment")
    private boolean hasSpecializedEquipment;

    @Column(name = "physical_demand_level")
    private int physicalDemandLevel;

    @Column(name = "prefers_low_intensity")
    private boolean prefersLowIntensity;

    @Column(name = "prefers_moderate_intensity")
    private boolean prefersModerateIntensity;

    @Column(name = "prefers_high_intensity")
    private boolean prefersHighIntensity;

    @Column(name = "prefers_short_duration")
    private boolean prefersShortDuration;

    @Column(name = "prefers_moderate_duration")
    private boolean prefersModerateDuration;

    @Column(name = "prefers_long_duration")
    private boolean prefersLongDuration;

    @Column(name = "goal_strength")
    private boolean goalStrength;

    @Column(name = "goal_endurance")
    private boolean goalEndurance;

    @Column(name = "goal_flexibility")
    private boolean goalFlexibility;

    @Column(name = "goal_balance_coordination")
    private boolean goalBalanceCoordination;

    @Column(name = "goal_mental_focus")
    private boolean goalMentalFocus;
}

```

Then we need to update `ApplicationUser.java` with link to `UserPreferences.java`:

```java
@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
private UserPreferences preferences;
```

We can calculate preference distances. We take all distances into account:

```python
def calculate_preference_distances(user_preferences):
    """
    Calculate preference distance matrix using all columns from the UserPreferences table.
    Assumes that user_preferences is a list of dictionaries where each row contains preferences.
    """
    # Convert to a DataFrame
    df = pd.DataFrame(user_preferences)

    # Exclude the identifier or foreign key columns
    excluded_columns = ["id", "applicationUserId"]

    # Select only numeric or boolean columns (ignoring excluded ones)
    preference_columns = [
        col for col in df.columns
        if col not in excluded_columns and pd.api.types.is_numeric_dtype(df[col])
    ]

    # Log which columns are used for preference distances
    print("Columns used for preference distance calculation:", preference_columns)

    # Extract preferences for clustering
    preferences = df[preference_columns].values

    # Compute the pairwise Euclidean distances
    preference_distances = squareform(pdist(preferences, metric='euclidean'))
    return preference_distances
```

3. Distances need to be normalized:

```python
def normalize(matrix):
    """Normalize matrix using Frobenius norm."""
    return matrix / np.sqrt(np.sum(matrix ** 2))
```

4. Combine distances with alpha parameter -> is it more important to cluster spatially close users together or ones that have similar preferences?

```python
def combine_distances(spatial_distances, preference_distances, alpha):
    """Combine spatial and preference distances."""
    spatial_norm = normalize(spatial_distances)
    preference_norm = normalize(preference_distances)
    combined = alpha * preference_norm + (1 - alpha) * spatial_norm
    return combined
```

5. Use pam clustering to cluster users based on combined preference and spatial distances and optimal number of clusters. We use gap statistic to determine optimal number of clusters, we have to try out multiple `k_max` values to see what works the best for our number of observations.

```python
import numpy as np
from sklearn_extra.cluster import KMedoids
from gap_statistic import OptimalK

def pam_clustering_with_gap_statistic(combined_distances, alpha_values, k_max=10, n_bootstrap=50):
    """
    Perform PAM clustering using combined distance matrices and find optimal clusters using the gap statistic.
    """
    results = {}

    for alpha in alpha_values:
        print(f"Processing alpha = {alpha:.1f}...")

        
        # GAP statistic to find optimal K
        optimal_k_finder = OptimalK(parallel_backend='joblib', n_jobs=-1)
        optimal_clusters = optimal_k_finder(combined_dist, clusterer=KMedoids, n_refs=n_bootstrap, max_clusters=k_max)

        print(f"Optimal number of clusters for alpha={alpha:.1f}: {optimal_clusters}")

        # Perform PAM clustering with optimal K
        pam = KMedoids(n_clusters=optimal_clusters, metric='precomputed', random_state=123)
        cluster_labels = pam.fit_predict(combined_distances)

        results[alpha] = {
            "optimal_clusters": optimal_clusters,
            "cluster_labels": cluster_labels
        }

    return results

alpha_values = np.arange(0.1, 1.0, 0.1)
results = pam_clustering_with_gap_statistic(combined_distances, alpha_values)
```

We can visualize the results to see how our users were clustered based on where they live and what they like. In following example each user is presented by the coordinates of their address:

```python
import matplotlib.pyplot as plt
import seaborn as sns

def plot_clusters(df, cluster_labels, title="PAM Clustering Results"):
    df["cluster"] = cluster_labels
    plt.figure(figsize=(10, 6))
    sns.scatterplot(x="longitude", y="latitude", hue="cluster", data=df, palette="Set2", s=100)
    plt.title(title)
    plt.show()

# Example
plot_clusters(data_with_locations, results[0.5]["cluster_labels"], title="Alpha = 0.5")

```
In our user table we need to add cluster column:

```java
@Column(name = "cluster")
private Integer cluster;
```

We need to store our python results in our `ApplicationUser.java` table. What I found out is that a simple connection with the database is enough for this:

```python
def write_clusters_to_db(cluster_results, user_ids):
    """
    Write cluster assignments to the ApplicationUser table.
    :param cluster_results: List of cluster assignments for users
    :param user_ids: List of user IDs corresponding to the clusters
    """
    # Connect to the database
    connection = engine.connect()
    metadata = MetaData()
    application_user = Table("application_user", metadata, autoload_with=engine)

    # Prepare data for update
    cluster_data = [{"applicationUserId": user_ids[i], "cluster": cluster_results[i]} for i in range(len(user_ids))]

    # Update each user's cluster
    for record in cluster_data:
        update_stmt = (
            update(application_user)
            .where(application_user.c.application_user_id == record["user_id"])
            .values(cluster=record["cluster"])
        )
        connection.execute(update_stmt)

    # Commit and close
    connection.commit()
    connection.close()
    print("Clusters successfully written to the database.")
```

Note: When we create clusters they are created in the order of the users in the table, so we just need to assign created clusters to users, user with id 0 to first cluster observation in the list and so on.

#### Make Suggestions

- At this point we have no user history, so we map user preferences to activity features (`goalStrength`, `rainCompatibility`, etc.).
- We use regression model to estimate a suitability score for activities based on how well them match cluster preferences.
- We have to try out multiple regression models to see which one is best suited: _Linear Regression, Random Forest, Ridge_.
- When model is trained, predict scores for all activities for each cluster and recommend top N activities.


We took user addresses into account when making user clusters, so now we don't want to recommend activities far away from "cluster center" to the users in the cluster.
- We will calculate a "central point" (mean latitude/longitude) for each user cluster.
- Use the geodesic distance to only include activities within a reasonable range (maybe 10 km).
- When we have found activities taking place at most 10km away from center of user cluster, we use approach from above to amke suggestions.

Example of user preferences:
```python
user_preferences = [
    {"userId": 1, "prefersIndividual": True, "goalStrength": False, "goalFlexibility": True, "isBeginner": True},
    {"userId": 2, "prefersIndividual": False, "goalStrength": True, "goalFlexibility": False, "isBeginner": False},
    {"userId": 3, "prefersTeam": True, "goalEndurance": True, "goalStrength": True, "isIntermediate": True},
    {"userId": 4, "prefersTeam": False, "goalEndurance": True, "goalStrength": False, "isAdvanced": True},
]
```

Example of activities:
```python
activities = [
    {"studioActivityId": 101, "isIndividual": True, "goalFlexibility": True, "isBeginner": True, "goalStrength": False},
    {"studioActivityId": 102, "isTeam": True, "goalStrength": True, "isBeginner": False, "goalEndurance": True},
    {"studioActivityId": 103, "goalStrength": True, "goalEndurance": True, "isAdvanced": True, "isIndividual": False},
    {"studioActivityId": 104, "goalEndurance": True, "rainCompatibility": True, "isIntermediate": True, "isTeam": True},
]
```

Synthetically generate  suitability scores based on preferences and features of an activity:

```python
def generate_synthetic_scores(user_pref, activity):
    score = 0
    for key in user_pref.keys():
        if key in activity and user_pref[key] == activity[key]:
            score += 1  # Add points for matching features
    return score
```
Then we have to train the model on this data:

```python
train_df = pd.DataFrame(train_data)

# Split features and target
X = train_df.drop(columns=["userId", "studioActivityId", "suitability_score"])
y = train_df["suitability_score"]

# Train regression model
model = LinearRegression()
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
model.fit(X_train, y_train)

# Evaluate model
y_pred = model.predict(X_test)
print("Model RMSE:", np.sqrt(mean_squared_error(y_test, y_pred)))
```

Here I showed Linear regression, but I'm 100% sure it won't be good enough. For capturing such non-linear relationships RandomForest and XGBoost are well suited.
We can use RMSE or R-Squared to check how good the model preforms. Please download my file here and read about RMSE and R-Squared metrics: [link](https://github.com/mpetojevic/Spatio-Temporal-Analysis-Of-Sustainability-Data/blob/main/Report.pdf)

Calculate Cluster center, so the mean long/lat of cluster:
```python
def calculate_cluster_center(user_preferences):
    """
    Calculate the geographic center (mean latitude, longitude) for users in a cluster.
    """
    latitudes = [user['latitude'] for user in user_preferences]
    longitudes = [user['longitude'] for user in user_preferences]
    return (np.mean(latitudes), np.mean(longitudes))
```

We then need to filter activities that are close to users in cluster. Maybe we find some other approach, but I think it's okay, because it's not hard filtering for recommendations,m but just not to show sth where user for suer won't go:

```python
def filter_activities_by_distance(cluster_center, activities, max_distance_km=10):
    """
    Filter activities based on proximity to the cluster center.
    """
    def is_within_distance(activity):
        # this is ofc wrong, we have to collect this data from studio table, not studio activity, but the idea is clear
        activity_location = (activity['latitude'], activity['longitude'])
        distance = geodesic(cluster_center, activity_location).kilometers
        return distance <= max_distance_km

    return [activity for activity in activities if is_within_distance(activity)]
```

Now we have everything we need to recommend top N activities based on preferences, and user locations to a cluster of users:

```python
def recommend_activities_for_cluster(cluster_preferences, user_preferences, activities, model, top_n=5, max_distance_km=10):
    """
    Recommend top-N activities for a cluster, filtering by distance and scoring suitability.
    """
    # Step 1: Calculate cluster center
    cluster_center = calculate_cluster_center(user_preferences)

    # Step 2: Filter activities by distance
    nearby_activities = filter_activities_by_distance(cluster_center, activities, max_distance_km)
    if not nearby_activities:
        return []  # No activities nearby, maybe bad if we have a cluster of users that lives in 23. bezirk but studios and activities only in Donaustadt, that's why we might need another approach or some extra checks

    # Step 3: Prepare features for ML scoring
    activity_features = pd.DataFrame(nearby_activities)
    cluster_features = pd.DataFrame([cluster_preferences] * len(activity_features))
    input_features = pd.concat([cluster_features.reset_index(drop=True), activity_features.reset_index(drop=True)], axis=1)
    
    # Step 4: Predict suitability scores
    scores = model.predict(input_features.drop(columns=["studioActivityId", "latitude", "longitude"]))
    activity_features["predicted_score"] = scores

    # Step 5: Return top-N activities
    return activity_features.sort_values(by="predicted_score", ascending=False).head(top_n)[["studioActivityId", "name", "predicted_score"]]
```