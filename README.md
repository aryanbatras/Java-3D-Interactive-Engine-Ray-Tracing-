# 🌟 JavaReflect — 3D Interactive Ray Tracing Engine

![JavaReflect Banner](https://github.com/aryanbatras/JavaReflect-3D-Engine/blob/main/zap/mirror.jpg)

> **JavaReflect** is a 3D interactive ray tracing engine built **entirely from scratch** in Java. It features realistic rendering, user interaction, procedural scenes, object dragging, and multithreaded performance — all in a single powerful, extensible codebase.

---

## 🚀 Overview

JavaReflect simulates the physics of light using pure **ray tracing** principles in a self-built 3D environment. From photon-like rays bouncing off complex surfaces to drag-and-drop interaction and full camera control, this engine showcases the power of **modern Java** in graphical computing.

With **over 5,000 lines of handwritten code**, no third-party engines, and real-time interaction, this is more than a ray tracer — it’s a **learning tool**, a **sandbox**, and a **platform for 3D innovation**.

---

## 🎯 Features

### 🔷 Core Engine
- ✅ **Ray tracing** from scratch (primary, reflection, shadows planned)
- ✅ **Multithreaded** rendering loop (fast, scalable)
- ✅ **Antialiasing** with random sampling
- ✅ **Camera** with free movement, 360° mouse control, zoom, and orientation
- ✅ **Ground plane**, multiple visible objects, materials, and scene realism

### 🔷 Object System
- ✅ Spheres, Planes, Triangles, Boxes, Cones, Cylinders
- ✅ Unified object interface for movement, hit detection, scaling, center & radius manipulation
- ✅ Collision detection support between **any** shape combinations
- ✅ Mouse selection + dragging (intuitive user interaction)
- ✅ Fully OO design for extensibility

### 🔷 Scene & World
- ✅ Procedural scene generation with tunable randomness:
  - Sphere count, radius, position, fuzziness, materials, etc.
- ✅ Reflective ground support
- ✅ Reusable scene loaders
- ✅ Clean camera injection into render pipeline

### 🔷 Interface & Interaction
- ✅ **AWT-based Java window**
- ✅ Keyboard + Mouse control
- ✅ Realtime interaction and live rendering updates
- ✅ Scene redraw on interaction or camera shift
- ✅ Camera & object debug information printing

---

## 🌐 Screenshots

### 🎯 Highlight  
![Selected](https://github.com/aryanbatras/JavaReflect-3D-Engine/blob/main/zap/THEFINALSETUP.jpg)

### 🌀 Mirror Reflections  
![Mirror Scene](https://github.com/aryanbatras/JavaReflect-3D-Engine/blob/main/zap/REFLECTSCENE.jpg)

### ⚙️ Interactive Drag & Drop  
![Dragging Objects](https://github.com/aryanbatras/JavaReflect-3D-Engine/blob/main/zap/Screenshot%202025-05-30%20at%209.12.49%E2%80%AFAM.png)


---

## 💻 Getting Started

### 🛠️ Requirements

- Java 17+ (developed on **OpenJDK 23**)
- No external libraries required
- Optional: IDE (e.g. IntelliJ IDEA, Eclipse)

### ▶️ Run the Engine

```bash
# Compile
javac -d out src/**/*.java

# Run
java -cp out

```

---

## 🛣️ Roadmap

### Feature Status

- ✅ Basic ray tracing (spheres, planes)	Done
- ✅ Camera control with mouse + keyboard	Done
- ✅ Interactive object dragging		Done
- ✅ Material-based shading			Done
- ✅ Scene procedural randomization		Done
- ✅ Multithreaded rendering			Done
- ⏳ BVH acceleration				Upcoming
- ⏳ glTF 3D model support			Upcoming
- ⏳ Soft shadows & GI				Planned
- ⏳ Texture mapping				Planned
- ⏳ GUI-based scene editor			Planned
- ⏳ Real-time animation support		Planned

---

## 🤝 Contributing

We welcome contributions from developers, passionate about graphics, Java, or real-time engines.

### 🧾 How to Contribute
- Fork the repository
- Create a new branch: git checkout -b feature/my-feature
- Make your changes
- Commit and push: git commit -m "Added my feature" then git push origin

### Open a Pull Request 🚀
- 📌 Areas You Can Help
- 🔁 Refactor for cleaner OOP
- 🔼 Add new shape types or lighting models
- 🚀 Improve rendering performance (BVH, KD-Tree, SIMD)
- 🎨 Build a JavaFX UI wrapper
- 📦 Export/import scenes
- 📝 Write tutorials or docs
- 📚 New? Start from Window.java

## 📜 License

MIT License — use freely, modify, contribute, and credit.

## ✍️ Author

Developed by Aryan Batra
Contact: batraaryan03@gmail.com
Feel free to reach out for collaboration, internships, or tech discussions!

## ⭐ Star & Share

If you found JavaReflect useful or inspiring, consider starring ⭐ the project and sharing it!
Let’s grow this into the best open-source ray tracer written in Java!
