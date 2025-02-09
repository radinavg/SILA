from numpy import exp
from datetime import datetime


def calculate_time_decay(timestamp, decay_value=0.1):
    """
    calculates the time decay for an activity. In other words, based on
    the activity's timestamp, it calculates the relevance of the activity
    over time.

    :param timestamp: the beginning time of an activity.
    :param decay_value: decay rate, controlling how quickly the weight decreases over time.
    :return: the calculated weight
    """

    time_diff = (datetime.now() - timestamp).total_seconds() / (60 * 60 * 24)
    return exp(-decay_value * time_diff)
