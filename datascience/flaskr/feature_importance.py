import pandas as pd
import xgboost as xgb

# Sample code to compute feature importance per cluster
def calculate_feature_importance_per_cluster(data, clusters):
    """
    Calculate feature importance for each cluster using XGBoost.
    """
    importance_list = []

    for cluster in clusters:
        # Filter data for the current cluster
        cluster_data = data[data['cluster'] == cluster]
        if cluster_data.empty:
            continue

        # Separate features and labels
        X = cluster_data.drop(columns=['cluster', 'label'])
        y = cluster_data['label']

        # Train an XGBoost model
        dmatrix = xgb.DMatrix(data=X, label=y)
        model = xgb.train(
            params={
                "objective": "reg:squarederror",
                "verbosity": 0,
            },
            dtrain=dmatrix,
            num_boost_round=50
        )

        # Extract feature importance
        importance = model.get_score(importance_type='weight')
        importance_df = pd.DataFrame(list(importance.items()), columns=['Feature', 'Importance'])
        importance_df['Cluster'] = cluster
        importance_list.append(importance_df)

    return pd.concat(importance_list, ignore_index=True)
