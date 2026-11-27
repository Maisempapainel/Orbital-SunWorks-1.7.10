package com.orbitalsunworks.cinematic;

public final class CinematicTimeline {

    public static final int DURATION = 470;

    public static final int F0_ORBIT = 0;
    public static final int F1_FAIRING = 95;
    public static final int F2_SEPARATION = 160;
    public static final int F3_DRIFT = 235;
    public static final int F4_UNFOLD = 280;
    public static final int F5_INSPECT = 395;

    private CinematicTimeline() {
    }

    public static float clamp01(float v) {
        return v < 0f ? 0f : (v > 1f ? 1f : v);
    }

    public static float easeIn(float t) {
        float c = clamp01(t);
        return c * c * c;
    }

    public static float easeOut(float t) {
        float u = 1f - clamp01(t);
        return 1f - u * u * u;
    }

    public static float easeInOut(float t) {
        t = clamp01(t);
        return t < 0.5f ? 4f * t * t * t : 1f - (float) Math.pow(-2f * t + 2f, 3f) / 2f;
    }

    public static float spring(float t) {
        return easeInOut(t);
    }

    public static float mainBurn(float t) {
        if (t < 0f || t >= 82f) {
            return 0f;
        }
        if (t < 70f) {
            return 1f;
        }
        return easeOut(1f - (t - 70f) / 12f);
    }

    public static float nozzleHeat(float t) {
        if (t <= 72f) {
            return 1f;
        }
        return clamp01(1f - (t - 72f) / 65f);
    }

    public static float fairingRelease(float t) {
        return easeIn((t - F1_FAIRING) / 28f);
    }

    public static float fairingDrift(float t) {
        return spring((t - (F1_FAIRING + 8f)) / 62f) * 7.5f
            + Math.max(0f, t - (F1_FAIRING + 70f)) * 0.015f;
    }

    public static float fairingSpin(float t) {
        return clamp01((t - F1_FAIRING) / 65f) * 110f
            + Math.max(0f, t - (F1_FAIRING + 65f)) * 0.25f;
    }

    public static float separationPush(float t) {
        return spring((t - F2_SEPARATION) / 50f);
    }

    public static float satelliteDistance(float t) {
        float push = separationPush(t);
        return push * 1.4f + Math.max(0f, t - (F2_SEPARATION + 50f)) * 0.014f;
    }

    public static float stageRecede(float t) {
        float burn = easeIn((t - (F2_SEPARATION + 8f)) / 42f);
        return burn * 1.15f + Math.max(0f, t - (F2_SEPARATION + 50f)) * 0.024f;
    }

    public static float stageBurn(float t) {
        float a = (t - (F2_SEPARATION + 8f)) / 36f;
        if (a <= 0f || a >= 1.15f) {
            return 0f;
        }
        return a < 0.25f ? a / 0.25f : easeOut(clamp01((1.15f - a) / 0.9f));
    }

    public static float antennaDeploy(float t) {
        return easeInOut((t - F4_UNFOLD) / 48f);
    }

    public static float unfold(float t) {
        return easeInOut((t - F4_UNFOLD) / 90f);
    }

    public static float yokeOpen(float t, int side) {
        float delay = side < 0 ? 0f : 10f;
        return easeInOut((t - (F4_UNFOLD + 10f + delay)) / 32f);
    }

    public static float segmentExtend(float t, int side, int segment) {
        float segDelay = 20f + segment * 8f;
        float start = F4_UNFOLD + (side < 0 ? 0f : 10f) + segDelay;
        return easeInOut((t - start) / 26f);
    }

    public static float satelliteSpin(float t) {
        return Math.max(0f, t - F2_SEPARATION) * 0.40f;
    }

    public static float cameraFocusSatellite(float t) {
        return easeInOut((t - (F3_DRIFT - 18f)) / 42f);
    }

    public static float cameraDistance(float t) {
        float approach = easeOut((t - 10f) / 140f);
        float retreat = easeInOut((t - F5_INSPECT) / 60f);
        return 10.8f - 6.0f * approach + 1.6f * retreat;
    }

    public static float cameraOrbit(float t) {
        float base = 0.55f + t * 0.0028f;
        float inspect = easeInOut((t - F5_INSPECT) / 65f) * 1.45f;
        return base + inspect;
    }

    public static float cameraHeight(float t) {
        float approach = easeOut((t - 10f) / 140f);
        float settle = easeInOut((t - F3_DRIFT) / 50f);
        return 3.6f - 1.8f * approach - 0.7f * settle;
    }

    public static String validate() {
        final float dt = 0.25f;

        float prevU = -1f;
        for (float t = 0f; t <= DURATION; t += dt) {
            float u = unfold(t);
            if (u < prevU - 1e-6f) {
                return "unfold regrediu em t=" + t;
            }
            if (u < 0f || u > 1f) {
                return "unfold fora de [0,1] em t=" + t + " (" + u + ")";
            }
            prevU = u;
        }
        for (int side = -1; side <= 1; side += 2) {
            for (int seg = 0; seg < 6; seg++) {
                float prev = -1f;
                for (float t = 0f; t <= DURATION; t += dt) {
                    float v = segmentExtend(t, side, seg);
                    if (v < prev - 1e-6f) {
                        return "segmento " + side + "/" + seg + " regrediu em t=" + t;
                    }
                    if (v < 0f || v > 1f) {
                        return "segmento fora de [0,1]: " + side + "/" + seg;
                    }
                    prev = v;
                }
            }
            float prevY = -1f;
            for (float t = 0f; t <= DURATION; t += dt) {
                float v = yokeOpen(t, side);
                if (v < prevY - 1e-6f) {
                    return "yoke " + side + " regrediu em t=" + t;
                }
                prevY = v;
            }
        }

        float prevD = satelliteDistance(0f);
        for (float t = dt; t <= DURATION; t += dt) {
            float d = satelliteDistance(t);
            if (d - prevD > 0.10f * dt) {
                return "separacao brusca em t=" + t;
            }
            if (d < prevD - 1e-6f) {
                return "separacao regrediu em t=" + t;
            }
            prevD = d;
        }

        float prevR = stageRecede(0f);
        for (float t = dt; t <= DURATION; t += dt) {
            float r = stageRecede(t);
            if (r < prevR - 1e-6f) {
                return "estagio regrediu em t=" + t;
            }
            if (r - prevR > 0.12f * dt) {
                return "estagio recuou bruscamente em t=" + t;
            }
            prevR = r;
        }

        float prevDist = cameraDistance(0f);
        float prevH = cameraHeight(0f);
        for (float t = dt; t <= DURATION; t += dt) {
            float d = cameraDistance(t);
            float h = cameraHeight(t);
            if (Math.abs(d - prevDist) > 0.6f * dt * 8f) {
                return "camera teleportou (dist) em t=" + t;
            }
            if (Math.abs(h - prevH) > 0.6f * dt * 8f) {
                return "camera teleportou (alt) em t=" + t;
            }
            prevDist = d;
            prevH = h;
        }

        for (float t = 0f; t <= DURATION; t += dt) {
            float v = fairingRelease(t);
            if (v < 0f || v > 1f) {
                return "fairingRelease fora de [0,1] em t=" + t;
            }
            v = cameraFocusSatellite(t);
            if (v < 0f || v > 1f) {
                return "cameraFocus fora de [0,1] em t=" + t;
            }
            v = stageBurn(t);
            if (v < 0f || v > 1f) {
                return "stageBurn fora de [0,1] em t=" + t;
            }
        }

        if (!(F0_ORBIT < F1_FAIRING && F1_FAIRING < F2_SEPARATION
                && F2_SEPARATION < F3_DRIFT && F3_DRIFT < F4_UNFOLD
                && F4_UNFOLD < F5_INSPECT && F5_INSPECT < DURATION)) {
            return "fases fora de ordem";
        }

        return null;
    }
}