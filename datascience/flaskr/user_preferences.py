class UserPreferences:

    def __init__(self, df_row):
        """
        Initialize UserPreferences from a DataFrame row.
        """
        self.user_id = df_row["application_user_id"]
        self.preferred_activity_type = {
            "individual": df_row["prefers_individual"],
            "team": df_row["prefers_team"],
            "waterBased": df_row["prefers_water_based"],
            "indoor": df_row["prefers_indoor"],
            "outdoor": df_row["prefers_outdoor"],
            "bothIndoorAndOutdoor": df_row["prefers_both_indoor_and_outdoor"],
        }
        self.weather_suitability = {
            "warmClimate": df_row["prefers_warm_climate"],
            "coldClimate": df_row["prefers_cold_climate"],
            "rainCompatible": df_row["rain_compatibility"],
            "windSuitable": df_row["wind_suitability"],
        }
        self.muscle_group_focus = {
            "upperBody": df_row["focus_upper_body"],
            "lowerBody": df_row["focus_lower_body"],
            "core": df_row["focus_core"],
            "fullBody": df_row["focus_full_body"],
        }
        self.skill_level = {
            "beginner": df_row["is_beginner"],
            "intermediate": df_row["is_intermediate"],
            "advanced": df_row["is_advanced"],
        }
        self.physical_demand = df_row["physical_demand_level"]
        self.fitness_goals = {
            "strength": df_row["goal_strength"],
            "endurance": df_row["goal_endurance"],
            "flexibility": df_row["goal_flexibility"],
            "balanceCoordination": df_row["goal_balance_coordination"],
            "mentalFocus": df_row["goal_mental_focus"],
        }

    def to_vector(self):
        """
        Flatten user preferences into a vector.
        """

        vector = []
        # Activity Type
        vector.extend([
            int(self.preferred_activity_type["individual"]),
            int(self.preferred_activity_type["team"]),
            int(self.preferred_activity_type["waterBased"])
        ])
        vector.extend([
            int(self.preferred_activity_type["indoor"]),
            int(self.preferred_activity_type["outdoor"]),
            int(self.preferred_activity_type["bothIndoorAndOutdoor"])
        ])
        # Weather Preferences
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
        # Physical Demand Preference
        vector.append(self.physical_demand / 10.0)
        # Fitness Goals
        vector.extend([
            int(self.fitness_goals["strength"]),
            int(self.fitness_goals["endurance"]),
            int(self.fitness_goals["flexibility"]),
            int(self.fitness_goals["balanceCoordination"]),
            int(self.fitness_goals["mentalFocus"])
        ])

        # print(f"User preferences for user {self.user_id}: {vector}")
        return vector
