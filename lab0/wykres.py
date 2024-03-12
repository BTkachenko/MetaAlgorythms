import matplotlib.pyplot as plt
import os

# Function to read results from the result files
# Function to read results from the result files
def read_results(file_path):
    results = {}
    with open(file_path, 'r') as f:
        lines = f.readlines()
        for line in lines:
            key, value = line.strip().split(": ")
            results[key] = round(float(value))  # Rounding the float value
    return results

# Initialize an empty dictionary to hold all results
all_results = {}

# Loop through each result file and read the results
result_files = [f for f in os.listdir(".") if f.startswith("result")]
for result_file in result_files:
    file_path = os.path.join(".", result_file)
    all_results[result_file] = read_results(file_path)

# Plotting
labels = list(all_results.keys())
avg_of_ten = [all_results[label]['Average of 10-minimum groups'] for label in labels]
avg_of_fifty = [all_results[label]['Average of 50-minimum groups'] for label in labels]
overall_min = [all_results[label]['Overall minimum'] for label in labels]

x = range(len(labels))  # the label locations

fig, ax = plt.subplots(figsize=(20, 10))  # Increased figure size

rects1 = ax.bar(x, avg_of_ten, width=0.2, label='Average of 10-minimum groups')
rects2 = ax.bar([p + 0.2 for p in x], avg_of_fifty, width=0.2, label='Average of 50-minimum groups')
rects3 = ax.bar([p + 0.4 for p in x], overall_min, width=0.2, label='Overall minimum')

# Add some text for labels, title and custom x-axis tick labels, etc.
ax.set_ylabel('Scores')
ax.set_title('Scores by group')
ax.set_xticks([p + 0.2 for p in x])
ax.set_xticklabels(labels, rotation=45, fontsize=10)  # Rotated and reduced font size
ax.legend()

# Autolabel function to display labels on top of the bars
def autolabel(rects):
    for rect in rects:
        height = rect.get_height()
        ax.annotate('{}'.format(height),
                    xy=(rect.get_x() + rect.get_width() / 2, height),
                    xytext=(0, 3),  # 3 points vertical offset
                    textcoords="offset points",
                    ha='center', va='bottom')

autolabel(rects1)
autolabel(rects2)
autolabel(rects3)

plt.savefig('wykres-wynikow.png')  # Save the figure as a PNG file
