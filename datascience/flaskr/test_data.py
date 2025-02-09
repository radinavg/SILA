from .content_based_filtering import ContentBasedFiltering
from .user_preferences import UserPreferences
from .activity_attributes import ActivityAttributes
import random

activity_attributes_data = [
    ActivityAttributes(
        activity_id=1,
        activity_type={
            "individual": True,
            "team": False,
            "waterBased": False,
            "setting": "indoor"
        },
        weather_suitability={
            "warmClimate": True,
            "coldClimate": True,
            "rainCompatible": False,
            "windSuitable": False
        },
        muscle_group_focus={
            "upperBody": False,
            "lowerBody": False,
            "core": True,
            "fullBody": True
        },
        skill_level={
            "beginner": True,
            "intermediate": False,
            "advanced": False
        },
        equipment_requirements={
            "minimalEquipment": True,
            "specializedEquipment": True
        },
        physical_demand=2,
        intensity={
            "low": True,
            "moderate": False,
            "high": False
        },
        duration={
            "short": True,
            "moderate": True,
            "long": False
        },
        training_goals={
            "strength": True,
            "endurance": False,
            "flexibility": True,
            "balanceCoordination": True,
            "mentalFocus": True
        }
    ),
    ActivityAttributes(
        activity_id=2,
        activity_type={
            "individual": False,
            "team": True,
            "waterBased": False,
            "setting": "outdoor"
        },
        weather_suitability={
            "warmClimate": True,
            "coldClimate": False,
            "rainCompatible": False,
            "windSuitable": True
        },
        muscle_group_focus={
            "upperBody": True,
            "lowerBody": True,
            "core": True,
            "fullBody": True
        },
        skill_level={
            "beginner": False,
            "intermediate": True,
            "advanced": True
        },
        equipment_requirements={
            "minimalEquipment": False,
            "specializedEquipment": True
        },
        physical_demand=8,
        intensity={
            "low": False,
            "moderate": False,
            "high": True
        },
        duration={
            "short": False,
            "moderate": True,
            "long": True
        },
        training_goals={
            "strength": True,
            "endurance": True,
            "flexibility": False,
            "balanceCoordination": True,
            "mentalFocus": False
        }
    ),
    ActivityAttributes(
        activity_id=3,
        activity_type={
            "individual": True,
            "team": False,
            "waterBased": True,
            "setting": "both"
        },
        weather_suitability={
            "warmClimate": True,
            "coldClimate": False,
            "rainCompatible": True,
            "windSuitable": False
        },
        muscle_group_focus={
            "upperBody": True,
            "lowerBody": False,
            "core": False,
            "fullBody": True
        },
        skill_level={
            "beginner": True,
            "intermediate": False,
            "advanced": False
        },
        equipment_requirements={
            "minimalEquipment": True,
            "specializedEquipment": False
        },
        physical_demand=4,
        intensity={
            "low": True,
            "moderate": True,
            "high": False
        },
        duration={
            "short": True,
            "moderate": False,
            "long": False
        },
        training_goals={
            "strength": False,
            "endurance": True,
            "flexibility": True,
            "balanceCoordination": False,
            "mentalFocus": True
        }
    )
]

user_preferences_data = [
    UserPreferences(
        user_id=2,
        preferred_activity_type={
            "individual": True,
            "team": True,
            "waterBased": False,
            "setting": "indoor"
        },
        weather={
            "warmClimate": True,
            "coldClimate": False,
            "rainCompatible": False,
            "windSuitable": False
        },
        muscle_group_focus={
            "upperBody": False,
            "lowerBody": False,
            "core": True,
            "fullBody": True
        },
        skill_level={
            "beginner": True,
            "intermediate": False,
            "advanced": False
        },
        equipment_accessibility={
            "minimalEquipment": True,
            "specializedEquipment": True
        },
        physical_demand={
            "demandLevel": 1
        },
        intensity={
            "low": True,
            "moderate": False,
            "high": True
        },
        duration={
            "short": True,
            "moderate": False,
            "long": False
        },
        fitness_goals={
            "strength": True,
            "endurance": False,
            "flexibility": False,
            "balanceCoordination": True,
            "mentalFocus": False
        }
    ),
    UserPreferences(
        user_id=3,
        preferred_activity_type={
            "individual": True,
            "team": False,
            "waterBased": False,
            "setting": "both"
        },
        weather={
            "warmClimate": True,
            "coldClimate": False,
            "rainCompatible": False,
            "windSuitable": False
        },
        muscle_group_focus={
            "upperBody": False,
            "lowerBody": True,
            "core": True,
            "fullBody": True
        },
        skill_level={
            "beginner": True,
            "intermediate": False,
            "advanced": False
        },
        equipment_accessibility={
            "minimalEquipment": True,
            "specializedEquipment": True
        },
        physical_demand={
            "demandLevel": 2
        },
        intensity={
            "low": False,
            "moderate": False,
            "high": False
        },
        duration={
            "short": True,
            "moderate": False,
            "long": False
        },
        fitness_goals={
            "strength": True,
            "endurance": False,
            "flexibility": True,
            "balanceCoordination": True,
            "mentalFocus": False
        }
    ),
    UserPreferences(
        user_id=5,
        preferred_activity_type={
            "individual": True,
            "team": True,
            "waterBased": True,
            "setting": "indoor"
        },
        weather={
            "warmClimate": True,
            "coldClimate": False,
            "rainCompatible": False,
            "windSuitable": True
        },
        muscle_group_focus={
            "upperBody": False,
            "lowerBody": False,
            "core": False,
            "fullBody": False
        },
        skill_level={
            "beginner": False,
            "intermediate": True,
            "advanced": False
        },
        equipment_accessibility={
            "minimalEquipment": True,
            "specializedEquipment": True
        },
        physical_demand={
            "demandLevel": 1
        },
        intensity={
            "low": True,
            "moderate": False,
            "high": False
        },
        duration={
            "short": False,
            "moderate": True,
            "long": True
        },
        fitness_goals={
            "strength": True,
            "endurance": True,
            "flexibility": True,
            "balanceCoordination": True,
            "mentalFocus": True
        }
    )
]




def generate_random_user_preferences(user_id):
    return UserPreferences(
        user_id=user_id,
        preferred_activity_type={
            "individual": random.choice([True, False]),
            "team": random.choice([True, False]),
            "waterBased": random.choice([True, False]),
            "setting": random.choice(["indoor", "outdoor", "both"]),
        },
        weather={
            "warmClimate": random.choice([True, False]),
            "coldClimate": random.choice([True, False]),
            "rainCompatible": random.choice([True, False]),
            "windSuitable": random.choice([True, False]),
        },
        muscle_group_focus={
            "upperBody": random.choice([True, False]),
            "lowerBody": random.choice([True, False]),
            "core": random.choice([True, False]),
            "fullBody": random.choice([True, False]),
        },
        skill_level={
            "beginner": random.choice([True, False]),
            "intermediate": random.choice([True, False]),
            "advanced": random.choice([True, False]),
        },
        equipment_accessibility={
            "minimalEquipment": random.choice([True, False]),
            "specializedEquipment": random.choice([True, False]),
        },
        physical_demand={
            "demandLevel": random.randint(1, 10),
        },
        intensity={
            "low": random.choice([True, False]),
            "moderate": random.choice([True, False]),
            "high": random.choice([True, False]),
        },
        duration={
            "short": random.choice([True, False]),
            "moderate": random.choice([True, False]),
            "long": random.choice([True, False]),
        },
        fitness_goals={
            "strength": random.choice([True, False]),
            "endurance": random.choice([True, False]),
            "flexibility": random.choice([True, False]),
            "balanceCoordination": random.choice([True, False]),
            "mentalFocus": random.choice([True, False]),
        }
    )

def generate_n_random_user_preferences(n):
    users = [generate_random_user_preferences(user_id=i) for i in range(1, n + 1)]
    for user in users:
        print(user.to_vector())
    return users

algorithm = ContentBasedFiltering(user_preferences_data, activity_attributes_data)
print(algorithm.calculate_cosine_similarity())
print(algorithm.recommend_activities_for_user(5))