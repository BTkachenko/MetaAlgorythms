from math import sqrt
import heapq
import matplotlib.pyplot as plt

# Function to calculate the Euclidean distance between two points
def euclidean_distance(point1, point2):
    x1, y1 = point1
    x2, y2 = point2
    return int(round(sqrt((x2 - x1)**2 + (y2 - y1)**2)))

# Function to implement Prim's Algorithm to find MST
def prim_algorithm(graph, start_vertex):
    mst = {}
    total_weight = 0
    visited = set([start_vertex])
    edges = [(cost, start_vertex, to) for to, cost in graph[start_vertex].items()]
    heapq.heapify(edges)

    while edges:
        cost, frm, to = heapq.heappop(edges)
        if to not in visited:
            visited.add(to)
            mst[frm, to] = cost
            total_weight += cost
            for to_next, cost in graph[to].items():
                if to_next not in visited:
                    heapq.heappush(edges, (cost, to, to_next))
    
    return mst, total_weight

# Function to perform DFS and create a TSP cycle based on the MST
def dfs(start, graph, visited, cycle):
    visited.add(start)
    cycle.append(start)
    for neighbor in graph[start]:
        if neighbor not in visited:
            dfs(neighbor, graph, visited, cycle)

# Function to plot the graph, MST, and TSP cycle
def plot_graph(coordinates, edges=None, cycle=None, title="Graph"):
    x = [coord[0] for coord in coordinates.values()]
    y = [coord[1] for coord in coordinates.values()]

    plt.figure(figsize=(12, 8))
    plt.scatter(x, y, c='blue', s=50, zorder=5)
    
    if edges:
        for (start, end), _ in edges.items():
            x_values = [coordinates[start][0], coordinates[end][0]]
            y_values = [coordinates[start][1], coordinates[end][1]]
            plt.plot(x_values, y_values, 'r--', lw=1, zorder=1)

    if cycle:
        for i in range(len(cycle) - 1):
            start, end = cycle[i], cycle[i + 1]
            x_values = [coordinates[start][0], coordinates[end][0]]
            y_values = [coordinates[start][1], coordinates[end][1]]
            plt.plot(x_values, y_values, 'g-', lw=1, zorder=2)
    
    plt.title(title)
    plt.show()

# Read the .tsp file and extract relevant data
with open('xqf131.tsp', 'r') as f:
    lines = f.readlines()

coord_start = lines.index('NODE_COORD_SECTION\n') + 1
coord_end = lines.index('EOF\n')

coordinates = {}
for line in lines[coord_start:coord_end]:
    index, x, y = map(float, line.strip().split())
    coordinates[int(index)] = (x, y)

# Create the weighted graph
graph = {}
for i in coordinates:
    for j in coordinates:
        if i != j:
            if i not in graph:
                graph[i] = {}
            distance = euclidean_distance(coordinates[i], coordinates[j])
            graph[i][j] = distance

# Calculate MST using Prim's Algorithm
mst_prim, total_weight_mst_prim = prim_algorithm(graph, list(coordinates.keys())[0])

# Create a graph representation of the MST for easier traversal
mst_prim_graph = {}
for (node1, node2), _ in mst_prim.items():
    if node1 not in mst_prim_graph:
        mst_prim_graph[node1] = []
    if node2 not in mst_prim_graph:
        mst_prim_graph[node2] = []
    mst_prim_graph[node1].append(node2)
    mst_prim_graph[node2].append(node1)

# Initialize visited set and cycle list for Prim's MST
visited_prim = set()
cycle_prim = []

# Perform DFS to create the cycle based on Prim's MST
dfs(list(coordinates.keys())[0], mst_prim_graph, visited_prim, cycle_prim)

# Close the cycle by connecting the last node to the first
cycle_prim.append(cycle_prim[0])

# Calculate the total weight of the TSP cycle based on Prim's MST
total_weight_tsp_prim = sum(graph[cycle_prim[i]][cycle_prim[i+1]] for i in range(len(cycle_prim) - 1))

# Print the results to console
print(f"Total weight of MST: {total_weight_mst_prim}")
print("TSP cycle based on MST:", " -> ".join(map(str, cycle_prim)))
print(f"Total weight of TSP cycle based on MST: {total_weight_tsp_prim}")

# Plot the graph with MST and TSP cycle
plot_graph(coordinates, edges=mst_prim, cycle=cycle_prim, title="Graph with MST (Red) and TSP Cycle (Green)")
