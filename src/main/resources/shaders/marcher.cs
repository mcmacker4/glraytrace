#version 430 core

#define NUM_SPHERES 10
#define MAX_REFLECTIONS 100
#define MAX_MARCHING_STEPS 20

#define M_PI 3.1415926535897932384626433832795
#define M_PI_2 (M_PI / 2.0)
#define M_PI_4 (M_PI / 4.0)

#define FLT_MAX 3.402823466e+38

layout (binding = 0, rgba32f) uniform image2D dest;
layout (binding = 1) uniform sampler2D environment;

struct Ray {
    vec3 origin;
    vec3 direction;
};

struct Camera {
    vec3 position;
    mat4 viewmatrix;
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

float fastAtan(float x) {
    return M_PI/4 * x + 0.273 * x * (1 - abs(x));
}

float fastAtan2(float x, float y) {
    if (x == 0.0) {
        if (y > 0.0) {
            return M_PI_2;
        } else if (y < 0) {
            return -M_PI_2;
        }
    } else {
        if (abs(x) > abs(y)) {
            float z = y / x;
            if (x > 0.0) {
                return fastAtan(z);
            } else if (y >= 0.0) {
                return fastAtan(z) + M_PI;
            } else {
                return fastAtan(z) - M_PI;
            }
        } else {
            float z = x / y;
            if (y > 0.0) {
                return -fastAtan(z) + M_PI_2;
            } else {
                return -fastAtan(z) - M_PI_2;
            }
        }
    }
    return 0.0;
}

vec3 skyColor(vec3 dir) {
    float y = 1 - (dot(vec3(0, 1, 0), dir) * 0.5 + 0.5);
    float x = 0.5 + fastAtan2(dir.z, dir.x) / (2 * M_PI);
    return texture(environment, vec2(x, y)).xyz;
    //return vec3(1) * clamp(dot(dir, sunDir) * 0.5 + 0.5, 0, 1);
}

float distToSphere(Sphere sphere, vec3 pos) {
    return length(sphere.position - pos) - sphere.radius;
}

float smallestDist(vec3 pos, vec3 dir) {
    float dist = FLT_MAX;
    for (int i = 0; i < NUM_SPHERES; i++) {
        if (dot(normalize(spheres[i].position - pos), dir) >= 0.0)
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
    float smallest;
};

RayHit rayMarch(Ray ray) {
    float dist = smallestDist(ray.origin, ray.direction);
    float smallest = dist;
    do {
        ray.origin += ray.direction * dist;
        dist = smallestDist(ray.origin, ray.direction);
        smallest = min(smallest, dist);
        if (dist > (FLT_MAX / 1000))
            return RayHit(vec3(0), vec3(0), -1, smallest);
    } while (dist > 0.00001);
    ray.origin += ray.direction * dist;
    int sphIdx = closestSphere(ray.origin);
    vec3 normal = normalize(ray.origin - spheres[sphIdx].position);
    return RayHit(ray.origin, normal, sphIdx, smallest);
}

vec3 shootRay(Ray ray) {
    vec3 color = vec3(1);
    RayHit hit;
    float smallest = FLT_MAX;
    for (int reflection = 0; reflection < MAX_REFLECTIONS; reflection++) {
        hit = rayMarch(ray);
        if (hit.sphereIdx == -1) break;
        color = spheres[hit.sphereIdx].color;
        ray.direction = normalize(reflect(ray.direction, hit.normal));
        ray.origin = hit.position;
    }
    color *= skyColor(ray.direction);
    return color;
}

Ray outgoingRay(vec2 uv) {
    vec3 corner = vec3(-1 * camera.aspect, -1, -1.2);
    vec3 hor = vec3(2 * camera.aspect, 0, 0);
    vec3 ver = vec3(0, 2, 0);
    vec3 look = corner + hor * uv.x + ver * uv.y;
    look = (camera.viewmatrix * vec4(look, 0.0)).xyz;
    return Ray(camera.position, normalize(look));
}

layout (local_size_x = 32, local_size_y = 32) in;
void main() {

    ivec2 size = imageSize(dest);
    uvec2 texel = gl_GlobalInvocationID.xy;
    
    if (texel.x > size.x || texel.y > size.y) {
        return;
    }

    vec2 ss = vec2(gl_GlobalInvocationID.xy) / vec2(size);
    
    Ray ray = outgoingRay(ss);
    vec4 color = vec4(shootRay(ray), 1.0);

    imageStore(dest, ivec2(texel), color);
    
}