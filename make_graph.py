import matplotlib.pyplot as plt

x_coal_f = []
y_coal_f = []
plt.plot(x_coal_f, y_coal_f, color="red", marker="o", linestyle="solid", linewidth=1, markersize=5)

x_coal_m = []
y_coal_m = []
plt.plot(x_coal_m, y_coal_m, color="green", marker="o", linestyle="solid", linewidth=1, markersize=5)

x_pop = []
y_pop = []
plt.plot(x_pop, y_pop, color="blue", linestyle="solid", linewidth=1)

# TODO: Labels
# TODO: Log scale

plt.show()
plt.savefig('plot.png')