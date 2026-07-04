import * as THREE from 'https://cdn.jsdelivr.net/npm/three@0.165.0/build/three.module.js';

const canvas = document.getElementById('societyScene');

if (canvas) {
    const scene = new THREE.Scene();
    const camera = new THREE.PerspectiveCamera(45, 1, 0.1, 100);
    camera.position.set(8, 7, 12);
    camera.lookAt(0, 1.4, 0);

    const renderer = new THREE.WebGLRenderer({
        canvas,
        antialias: true,
        alpha: true
    });
    renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2));
    renderer.outputColorSpace = THREE.SRGBColorSpace;

    const group = new THREE.Group();
    scene.add(group);

    // --- Enhanced Lighting ---
    const ambient = new THREE.HemisphereLight(0xdbeafe, 0x0f172a, 2.8);
    scene.add(ambient);

    const keyLight = new THREE.DirectionalLight(0xffffff, 3);
    keyLight.position.set(7, 10, 5);
    scene.add(keyLight);

    const fillLight = new THREE.PointLight(0x67e8f9, 18, 18);
    fillLight.position.set(-5, 2, 5);
    scene.add(fillLight);

    // Accent rim light for depth
    const rimLight = new THREE.PointLight(0xa78bfa, 12, 16);
    rimLight.position.set(5, 1, -5);
    scene.add(rimLight);

    // --- Materials ---
    const baseMaterial = new THREE.MeshStandardMaterial({
        color: 0xf8fafc,
        roughness: 0.52,
        metalness: 0.08
    });
    const sideMaterial = new THREE.MeshStandardMaterial({
        color: 0xbdd7ff,
        roughness: 0.58,
        metalness: 0.04
    });
    const glassMaterial = new THREE.MeshStandardMaterial({
        color: 0x38bdf8,
        emissive: 0x0ea5e9,
        emissiveIntensity: 0.5,
        roughness: 0.25,
        metalness: 0.2
    });
    const roofMaterial = new THREE.MeshStandardMaterial({
        color: 0x22c55e,
        roughness: 0.45,
        metalness: 0.05
    });
    const roadMaterial = new THREE.MeshStandardMaterial({
        color: 0x111827,
        roughness: 0.7,
        metalness: 0.02
    });

    // --- Ground Plane with Reflection ---
    const groundGeometry = new THREE.CircleGeometry(8, 64);
    const groundMaterial = new THREE.MeshStandardMaterial({
        color: 0x1e293b,
        roughness: 0.85,
        metalness: 0.15,
        transparent: true,
        opacity: 0.4
    });
    const ground = new THREE.Mesh(groundGeometry, groundMaterial);
    ground.rotation.x = -Math.PI / 2;
    ground.position.y = -0.35;
    group.add(ground);

    // --- Plaza ---
    const plaza = new THREE.Mesh(
        new THREE.CylinderGeometry(5.8, 6.5, 0.32, 6),
        new THREE.MeshStandardMaterial({ color: 0x475569, roughness: 0.62, metalness: 0.03 })
    );
    plaza.position.y = -0.18;
    plaza.rotation.y = Math.PI / 6;
    group.add(plaza);

    // --- Tower Builder with Glowing Windows ---
    const allWindows = [];

    const makeTower = (x, z, height, floors, accentColor) => {
        const tower = new THREE.Group();
        const body = new THREE.Mesh(new THREE.BoxGeometry(1.42, height, 1.42), baseMaterial);
        body.position.y = height / 2;
        tower.add(body);

        const side = new THREE.Mesh(new THREE.BoxGeometry(0.08, height + 0.02, 1.5), sideMaterial);
        side.position.set(-0.75, height / 2, 0);
        tower.add(side);

        const roof = new THREE.Mesh(new THREE.BoxGeometry(1.7, 0.16, 1.7), roofMaterial);
        roof.position.y = height + 0.12;
        tower.add(roof);

        const windowMaterial = glassMaterial.clone();
        windowMaterial.emissive = new THREE.Color(accentColor);
        windowMaterial.color = new THREE.Color(accentColor);

        for (let floor = 0; floor < floors; floor += 1) {
            for (let col = 0; col < 2; col += 1) {
                const frontWindow = new THREE.Mesh(new THREE.BoxGeometry(0.28, 0.18, 0.035), windowMaterial);
                frontWindow.position.set(col === 0 ? -0.32 : 0.32, 0.55 + floor * 0.48, 0.73);
                tower.add(frontWindow);
                allWindows.push({ mesh: frontWindow, material: windowMaterial, baseIntensity: 0.5, offset: floor * 0.7 + col * 1.3 });

                const sideWindow = new THREE.Mesh(new THREE.BoxGeometry(0.035, 0.18, 0.28), windowMaterial);
                sideWindow.position.set(0.73, 0.55 + floor * 0.48, col === 0 ? -0.32 : 0.32);
                tower.add(sideWindow);
                allWindows.push({ mesh: sideWindow, material: windowMaterial, baseIntensity: 0.5, offset: floor * 0.9 + col * 2.1 });
            }
        }

        tower.position.set(x, 0, z);
        group.add(tower);
        return tower;
    };

    const towers = [
        makeTower(-2.2, -0.35, 3.9, 7, 0x38bdf8),
        makeTower(0.15, 0.5, 4.9, 9, 0xa7f3d0),
        makeTower(2.55, -0.7, 3.3, 6, 0xfde68a)
    ];

    // --- Road ---
    const road = new THREE.Mesh(new THREE.BoxGeometry(7.8, 0.06, 0.72), roadMaterial);
    road.position.set(0.15, 0.02, 2.58);
    road.rotation.y = -0.2;
    group.add(road);

    // --- Gate ---
    const gate = new THREE.Group();
    const pillarGeometry = new THREE.BoxGeometry(0.18, 0.8, 0.18);
    const gateMaterial = new THREE.MeshStandardMaterial({ color: 0xe2e8f0, roughness: 0.35 });
    [-0.65, 0.65].forEach((x) => {
        const pillar = new THREE.Mesh(pillarGeometry, gateMaterial);
        pillar.position.set(x, 0.42, 2.24);
        gate.add(pillar);
    });
    const beam = new THREE.Mesh(new THREE.BoxGeometry(1.55, 0.16, 0.2), gateMaterial);
    beam.position.set(0, 0.88, 2.24);
    gate.add(beam);
    group.add(gate);

    // --- Orbiting Rings ---
    const ringMaterial = new THREE.MeshBasicMaterial({
        color: 0x67e8f9,
        transparent: true,
        opacity: 0.32
    });
    const ringColors = [0x67e8f9, 0xa7f3d0, 0xfde68a];
    const rings = [1.55, 2.6, 3.65].map((radius, i) => {
        const mat = new THREE.MeshBasicMaterial({
            color: ringColors[i],
            transparent: true,
            opacity: 0.32
        });
        const ring = new THREE.Mesh(new THREE.TorusGeometry(radius, 0.008, 12, 120), mat);
        ring.rotation.x = Math.PI / 2;
        ring.position.y = 0.08;
        group.add(ring);
        return ring;
    });

    // --- Floating Particles ---
    const particleCount = 60;
    const particleGeometry = new THREE.BufferGeometry();
    const particlePositions = new Float32Array(particleCount * 3);
    const particleSizes = new Float32Array(particleCount);

    for (let i = 0; i < particleCount; i++) {
        particlePositions[i * 3] = (Math.random() - 0.5) * 16;
        particlePositions[i * 3 + 1] = Math.random() * 8;
        particlePositions[i * 3 + 2] = (Math.random() - 0.5) * 16;
        particleSizes[i] = Math.random() * 0.06 + 0.02;
    }

    particleGeometry.setAttribute('position', new THREE.BufferAttribute(particlePositions, 3));

    const particleMaterial = new THREE.PointsMaterial({
        color: 0x7dd3fc,
        size: 0.05,
        transparent: true,
        opacity: 0.6,
        blending: THREE.AdditiveBlending,
        depthWrite: false
    });

    const particles = new THREE.Points(particleGeometry, particleMaterial);
    scene.add(particles);

    // --- Resize ---
    const resize = () => {
        const width = canvas.clientWidth || window.innerWidth;
        const height = canvas.clientHeight || window.innerHeight;
        renderer.setSize(width, height, false);
        camera.aspect = width / height;
        camera.updateProjectionMatrix();
    };

    window.addEventListener('resize', resize);
    resize();

    // --- Pointer Tracking ---
    const pointer = { x: 0, y: 0, targetX: 0, targetY: 0 };
    window.addEventListener('pointermove', (event) => {
        pointer.targetX = (event.clientX / window.innerWidth - 0.5) * 0.35;
        pointer.targetY = (event.clientY / window.innerHeight - 0.5) * 0.25;
    });

    // --- CTA Hover Zoom ---
    let zoomTarget = 0;
    const ctaButtons = document.querySelectorAll('#btn-register, #btn-login');
    ctaButtons.forEach((btn) => {
        btn.addEventListener('pointerenter', () => { zoomTarget = 0.8; });
        btn.addEventListener('pointerleave', () => { zoomTarget = 0; });
    });
    let currentZoom = 0;

    // --- Animation Loop ---
    const clock = new THREE.Clock();
    const animate = () => {
        const elapsed = clock.getElapsedTime();

        // Smooth pointer interpolation (elastic feel)
        pointer.x += (pointer.targetX - pointer.x) * 0.06;
        pointer.y += (pointer.targetY - pointer.y) * 0.06;

        // Smooth zoom
        currentZoom += (zoomTarget - currentZoom) * 0.04;

        // Group rotation with elastic easing
        group.rotation.y = -0.45 + Math.sin(elapsed * 0.32) * 0.08 + pointer.x;
        group.rotation.x = -0.08 + pointer.y;

        // Camera zoom on CTA hover
        camera.position.z = 12 - currentZoom;
        camera.position.y = 7 - currentZoom * 0.3;

        // Tower breathing animation
        towers.forEach((tower, index) => {
            tower.position.y = Math.sin(elapsed * 1.2 + index) * 0.035;
        });

        // Window glow pulsing
        allWindows.forEach((w) => {
            const pulse = 0.3 + Math.sin(elapsed * 1.8 + w.offset) * 0.35;
            w.material.emissiveIntensity = Math.max(0.15, pulse);
        });

        // Ring rotation + color shift
        rings.forEach((ring, index) => {
            ring.rotation.z = elapsed * (0.08 + index * 0.025);
            const hue = (elapsed * 0.03 + index * 0.33) % 1;
            ring.material.color.setHSL(hue, 0.7, 0.7);
            ring.material.opacity = 0.2 + Math.sin(elapsed + index) * 0.06;
        });

        // Particle drift
        const positions = particles.geometry.attributes.position.array;
        for (let i = 0; i < particleCount; i++) {
            positions[i * 3 + 1] += 0.003;
            if (positions[i * 3 + 1] > 8) {
                positions[i * 3 + 1] = 0;
                positions[i * 3] = (Math.random() - 0.5) * 16;
                positions[i * 3 + 2] = (Math.random() - 0.5) * 16;
            }
        }
        particles.geometry.attributes.position.needsUpdate = true;

        // Particle twinkle
        particleMaterial.opacity = 0.45 + Math.sin(elapsed * 2) * 0.15;

        // Rim light orbit
        rimLight.position.x = Math.cos(elapsed * 0.3) * 6;
        rimLight.position.z = Math.sin(elapsed * 0.3) * 6;
        rimLight.intensity = 10 + Math.sin(elapsed * 0.8) * 4;

        renderer.render(scene, camera);
        requestAnimationFrame(animate);
    };

    animate();
}
