#version 430 core

#define NUM_SPHERES 10
#define REFLECTIONS 100

#define M_PI 3.1415926535897932384626433832795

layout (binding = 0, rgba32f) uniform image2D dest;
layout (binding = 1) uniform sampler2D environment;

struct Ray {
    vec3 origin;
    vec3 direction;
};

struct Camera {
    vec3 position;
    float aspect;
};

struct Sphere {
    vec3 position;
    float radius;
    vec3 color;
};

uniform Camera camera;
uniform Sphere spheres[NUM_SPHERES];

const vec3 sunDir = normalize(vec3(1, 1, 0));

vec3 skyColor(vec3 dir) {
    float y = 1 - (dot(vec3(0, 1, 0), dir) * 0.5 + 0.5);
    float x = 0.5 + atan(dir.z, dir.x) / (2 * M_PI);
    return texture(environment, vec2(x, y)).xyz;
    //return vec3(1) * clamp(dot(dir, sunDir) * 0.5 + 0.5, 0, 1);
}

float distToSphere(Sphere sphere, vec3 pos) {
    return length(sphere.position - pos) - sphere.radius;
}

float smallestDist(vec3 pos) {
    float dist = distToSphere(spheres[0], pos);
    for (int i = 1; i < NUM_SPHERES; i++) {
        dist = min(dist, distToSphere(spheres[i], pos));
    }
    return dist;
}

int closestSphere(vec3 pos) {
    int idx = 0;
    float dist = distToSphere(spheres[idx], pos);
    for (int i = 1; i < NUM_SPHERES; i++) {
        float newDist = distToSphere(spheres[i], pos);
        if (newDist < dist) {
            dist = newDist;
            idx = i;
        }
    }
    return idx;
}

struct RayHit {
    vec3 position;
    vec3 normal;
    int sphereIdx;
};

RayHit rayMarch(Ray ray) {
    float dist = smallestDist(ray.origin);
    do {
        ray.origin += ray.direction * dist;
        dist = smallestDist(ray.origin);
        if (dist > 10000)
            return RayHit(vec3(0), vec3(0), -1);
    } while (dist > 0.00001);
    ray.origin += ray.direction * dist;
    int sphIdx = closestSphere(ray.origin);
    vec3 normal = normalize(ray.origin - spheres[sphIdx].position);
    return RayHit(ray.origin, normal, sphIdx);
}

vec3 shootRay(Ray ray) {
    int reflection = 0;
    vec3 color = vec3(1);
    RayHit hit;
    while (reflection < REFLECTIONS) {
        hit = rayMarch(ray);
        if (hit.sphereIdx == -1) break;
        color *= spheres[hit.sphereIdx].color;
        ray.direction = normalize(reflect(ray.direction, hit.normal));
        ray.origin = hit.position + ray.direction * 0.00002;
        reflection++;
    }
    color *= skyColor(ray.direction);
    return color;
}

Ray outgoingRay(vec2 uv) {
    vec3 corner = vec3(-1 * camera.aspect, -1, -1);
    vec3 hor = vec3(2 * camera.aspect, 0, 0);
    vec3 ver = vec3(0, 2, 0);
    return Ray(camera.position, normalize(corner + hor * uv.x + ver * uv.y));
}

layout (local_size_x = 32, local_size_y = 32) in;
void main() {

    ivec2 size = imageSize(dest);
    ivec2 texel = ivec2(gl_GlobalInvocationID.xy);
    
    if (texel.x > size.x || texel.y > size.y) {
        return;
    }

    vec2 ss = vec2(gl_GlobalInvocationID.xy) / vec2(size);

    if (ss.x > 1 || ss.y > 1) {
        return;
    }
    
    Ray ray = outgoingRay(ss);
    vec4 color = vec4(shootRay(ray), 1.0);

    imageStore(dest, texel, color);
    
}