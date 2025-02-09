class ActivityAttributes:

    def __init__(self, df_row):
        """
        Initialize ActivityAttributes from a DataFrame row.
        """
        self.activity_id = df_row["id"]
        self.activity_type = {
            "individual": df_row["is_individual"],
            "team": df_row["is_team"],
            "waterBased": df_row["is_water_based"],
            "indoor": df_row["is_indoor"],
            "outdoor": df_row["is_outdoor"],
            "bothIndoorAndOutdoor": df_row["is_both_indoor_and_outdoor"],
        }
        self.weather_suitability = {
            "warmClimate": df_row["suitable_warm_climate"],
            "coldClimate": df_row["suitable_cold_climate"],
            "rainCompatible": df_row["rain_compatibility"],
            "windSuitable": df_row["wind_suitability"],
        }
        self.muscle_group_focus = {
            "upperBody": df_row["involves_upper_body"],
            "lowerBody": df_row["involves_lower_body"],
            "core": df_row["involves_core"],
            "fullBody": df_row["involves_full_body"],
        }
        self.skill_level = {
            "beginner": df_row["is_beginner"],
            "intermediate": df_row["is_intermediate"],
            "advanced": df_row["is_advanced"],
        }
        self.physical_demand = df_row["physical_demand_level"]
        self.training_goals = {
            "strength": df_row["goal_strength"],
            "endurance": df_row["goal_endurance"],
            "flexibility": df_row["goal_flexibility"],
            "balanceCoordination": df_row["goal_balance_coordination"],
            "mentalFocus": df_row["goal_mental_focus"],
        }

    def to_vector(self):
        """
        Convert activity attributes into a flat vector for numerical operations.
        """

        vector = []
        # Activity Type
        vector.extend([
            int(self.activity_type["individual"]),
            int(self.activity_type["team"]),
            int(self.activity_type["waterBased"])
        ])
        vector.extend([
            int(self.activity_type["indoor"]),
            int(self.activity_type["outdoor"]),
            int(self.activity_type["bothIndoorAndOutdoor"])
        ])
        # Weather Suitability
        vector.extend([
            int(self.weather_suitability["warmClimate"]),
            int(self.weather_suitability["coldClimate"]),
            int(self.weather_suitability["rainCompatible"]),
            int(self.weather_suitability["windSuitable"])
        ])
        # Muscle Group Focus
        vector.extend([
            int(self.muscle_group_focus["upperBody"]),
            int(self.muscle_group_focus["lowerBody"]),
            int(self.muscle_group_focus["core"]),
            int(self.muscle_group_focus["fullBody"])
        ])
        # Skill Level
        vector.extend([
            int(self.skill_level["beginner"]),
            int(self.skill_level["intermediate"]),
            int(self.skill_level["advanced"])
        ])
        # Physical Demand
        vector.append(self.physical_demand / 10.0)
        # Training Goals
        vector.extend([
            int(self.training_goals["strength"]),
            int(self.training_goals["endurance"]),
            int(self.training_goals["flexibility"]),
            int(self.training_goals["balanceCoordination"]),
            int(self.training_goals["mentalFocus"])
        ])

        # print(f"Activity attributes for activity {self.activity_id}: {vector}")
        return vector
