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

    const ambient = new THREE.HemisphereLight(0xdbeafe, 0x0f172a, 2.8);
    scene.add(ambient);

    const keyLight = new THREE.DirectionalLight(0xffffff, 3);
    keyLight.position.set(7, 10, 5);
    scene.add(keyLight);

    const fillLight = new THREE.PointLight(0x67e8f9, 18, 18);
    fillLight.position.set(-5, 2, 5);
    scene.add(fillLight);

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

    const plaza = new THREE.Mesh(
        new THREE.CylinderGeometry(5.8, 6.5, 0.32, 6),
        new THREE.MeshStandardMaterial({ color: 0x475569, roughness: 0.62, metalness: 0.03 })
    );
    plaza.position.y = -0.18;
    plaza.rotation.y = Math.PI / 6;
    group.add(plaza);

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

                const sideWindow = new THREE.Mesh(new THREE.BoxGeometry(0.035, 0.18, 0.28), windowMaterial);
                sideWindow.position.set(0.73, 0.55 + floor * 0.48, col === 0 ? -0.32 : 0.32);
                tower.add(sideWindow);
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

    const road = new THREE.Mesh(new THREE.BoxGeometry(7.8, 0.06, 0.72), roadMaterial);
    road.position.set(0.15, 0.02, 2.58);
    road.rotation.y = -0.2;
    group.add(road);

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

    const ringMaterial = new THREE.MeshBasicMaterial({
        color: 0x67e8f9,
        transparent: true,
        opacity: 0.32
    });
    const rings = [1.55, 2.6, 3.65].map((radius) => {
        const ring = new THREE.Mesh(new THREE.TorusGeometry(radius, 0.008, 12, 120), ringMaterial);
        ring.rotation.x = Math.PI / 2;
        ring.position.y = 0.08;
        group.add(ring);
        return ring;
    });

    const resize = () => {
        const width = canvas.clientWidth || window.innerWidth;
        const height = canvas.clientHeight || window.innerHeight;
        renderer.setSize(width, height, false);
        camera.aspect = width / height;
        camera.updateProjectionMatrix();
    };

    window.addEventListener('resize', resize);
    resize();

    const pointer = { x: 0, y: 0 };
    window.addEventListener('pointermove', (event) => {
        pointer.x = (event.clientX / window.innerWidth - 0.5) * 0.35;
        pointer.y = (event.clientY / window.innerHeight - 0.5) * 0.25;
    });

    const clock = new THREE.Clock();
    const animate = () => {
        const elapsed = clock.getElapsedTime();
        group.rotation.y = -0.45 + Math.sin(elapsed * 0.32) * 0.08 + pointer.x;
        group.rotation.x = -0.08 + pointer.y;
        towers.forEach((tower, index) => {
            tower.position.y = Math.sin(elapsed * 1.2 + index) * 0.035;
        });
        rings.forEach((ring, index) => {
            ring.rotation.z = elapsed * (0.08 + index * 0.025);
            ring.material.opacity = 0.2 + Math.sin(elapsed + index) * 0.04;
        });
        renderer.render(scene, camera);
        requestAnimationFrame(animate);
    };

    animate();
}
