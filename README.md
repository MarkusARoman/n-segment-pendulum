# n-pendulum
Github Project a Day Streak (III)

This project simulates an n-pendulum system and uses the position of the final pendulum point to generate a Julia fractal in real time using OpenGL dynamically.

![n-pendulum](https://github.com/user-attachments/assets/dd17fe5e-a9cd-4f85-af9e-68793436b6e0)

### Technologies Used
- Java 17+
- LWJGL 3
- OpenGL 3.3+
- GLSL shaders
- JOML
- Maven

## How it Works

### Java
The pendulum is updated 100 times per frame for smoother motion.
The final tip's position is normalized and passed as a complex number **c** into the Julia shader.
A line trail of the last N points is rendered with alpha fading.
A full-screen quad displays the Julia fractal in the background.

### GLSL
Pendulum Shader <br>
Responsible for rendering pendulum lines and trails with transparency and color fading.
<br> Julia Shader <br>
Used to generate the Julia set based on the pendulum's tip position.

## License
MIT License. Feel free to use and modify for personal or academic projects. Attribution appreciated!
