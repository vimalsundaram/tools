import math

@outputSchema('s_deviation:double')
def standard_deviation(input_bag):
    if input_bag is None or len(input_bag) == 0:
        return None

    num_values = [value[0] for value in input_bag]

    sum_lambda = lambda x, y: x + y
    avg = reduce(sum_lambda, num_values) / float(len(num_values))

    deviation_lambda = lambda x: math.pow(x - avg, 2)
    variance = reduce(sum_lambda, map(deviation_lambda, num_values)) / len(num_values)

    return math.sqrt(variance)
