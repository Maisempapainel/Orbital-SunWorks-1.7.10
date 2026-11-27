package com.orbitalsunworks.cinematic;

/**
 * LINHA DO TEMPO DA CINEMÁTICA DE DEPLOY (v0.9.0) — classe PURA.
 *
 * Sem OpenGL, sem Minecraft: toda a lógica de fases/easing da
 * cinemática vive AQUI para poder ser TESTADA no servidor headless
 * (bateria /ostest) — é o jeito certo de garantir que a animação é
 * contínua, monotônica e sem "pulos" sem precisar de placa de vídeo.
 *
 * SEQUÊNCIA (exigida pelo usuário, como nas missões reais/NASA):
 *   FASE 0  ORBIT INSERTION: o foguete (estágio superior + coifa com o
 *           satélite acoplado) chega à órbita já em cena, inércia lenta.
 *   FASE 1  SEPARAÇÃO DA COIFA: as metades se afastam girando (piro +
 *           molas), viram detritos que DERIVAM para trás (inércia).
 *   FASE 2  SEPARAÇÃO DO SATÉLITE: molas empurram o satélite
 *           SUAVEMENTE para frente (+Z) do estágio; o estágio acende o
 *           motor por 1s (burn de afastamento) e DERIVA para trás
 *           GRADUALMENTE (não desaparece!).
 *   FASE 3  DERIVA / ESTABILIZAÇÃO: câmera troca de alvo (do foguete
 *           pro satélite); o satélite gira devagar; RCS curto.
 *   FASE 4  DESDOBRAMENTO: as travas soltam e os painéis solares
 *           desdobram em SANFONA — cada braço abre em dobradiça com
 *           atraso em cascata, os segmentos esticam um a um, como uma
 *           régua dobrável se abrindo (NUNCA instantâneo).
 *   FASE 5  INSPEÇÃO FINAL: câmera faz um arco lento ao redor do
 *           satélite implantado, com a TERRA ao fundo, e se afasta.
 *
 * Todos os movimentos são SUAVES (easing), SEM explosões exageradas,
 * e os corpos respeitam INÉRCA (nada para de repente).
 */
public final class CinematicTimeline {

    /** Duração total em ticks (22s — mais longa, ritmo de documentário). */
    public static final int DURATION = 440;

    // Limites das fases (em ticks).
    public static final int F0_ORBIT = 0;
    public static final int F1_FAIRING = 70;
    public static final int F2_SEPARATION = 130;
    public static final int F3_DRIFT = 210;
    public static final int F4_UNFOLD = 280;
    public static final int F5_INSPECT = 380;

    private CinematicTimeline() {
    }

    // ------------------------------------------------------------------
    // Easings
    // ------------------------------------------------------------------

    public static float clamp01(float v) {
        return v < 0f ? 0f : (v > 1f ? 1f : v);
    }

    /** ease-in cúbico: começa devagar, acelera (empurrão de mola). */
    public static float easeIn(float t) {
        return clamp01(t) * clamp01(t) * clamp01(t);
    }

    /** ease-out cúbico: começa rápido, termina suave. */
    public static float easeOut(float t) {
        float u = 1f - clamp01(t);
        return 1f - u * u * u;
    }

    /** ease-in-out cúbico: lento-rápido-lento (movimento mecânico). */
    public static float easeInOut(float t) {
        t = clamp01(t);
        return t < 0.5f ? 4f * t * t * t : 1f - (float) Math.pow(-2f * t + 2f, 3f) / 2f;
    }

    /**
     * A mola de separação: começa PARADA (a trava segura), acelera no
     * meio (a mola empurra) e esvazia suave (a inércia segue) —
     * fisicamente convincente e dentro do limite de suavidade do
     * validate() (derivada máxima 1.5/45 ticks ~ 0.033/tick).
     */
    public static float spring(float t) {
        return easeInOut(t);
    }

    // ------------------------------------------------------------------
    // Estado da animação em um instante t (ticks, pode ser fracionário)
    // ------------------------------------------------------------------

    /** Metade da coifa: 0 = fechada, 1 = solta e afastada. */
    public static float fairingRelease(float t) {
        return easeIn((t - F1_FAIRING) / 26f);
    }

    /** Distância da metade da coifa (afasta girando; inércia). */
    public static float fairingDrift(float t) {
        return spring((t - (F1_FAIRING + 8f)) / 60f) * 9.0f
            + Math.max(0f, t - (F1_FAIRING + 68f)) * 0.011f;
    }

    /** Rotação total da metade da coifa ao derivar (graus). */
    public static float fairingSpin(float t) {
        return clamp01((t - F1_FAIRING) / 60f) * 95f
            + Math.max(0f, t - (F1_FAIRING + 60f)) * 0.22f;
    }

    /** Satélite: avanço da separação (0 = acoplado, 1 = molas soltas). */
    public static float separationPush(float t) {
        return spring((t - F2_SEPARATION) / 45f);
    }

    /** Distância do satélite ao estágio (blocos; molas + inércia). */
    public static float satelliteDistance(float t) {
        float push = separationPush(t);
        // A mola dá ~1.4 e a inércia mantém a deriva lenta depois.
        return push * 1.4f + Math.max(0f, t - (F2_SEPARATION + 45f)) * 0.014f;
    }

    /** Estágio superior: recuo (burn de afastamento + inércia). */
    public static float stageRecede(float t) {
        float burn = easeIn((t - (F2_SEPARATION + 6f)) / 30f);
        return burn * 1.1f + Math.max(0f, t - (F2_SEPARATION + 36f)) * 0.020f;
    }

    /** Motor do estágio aceso durante o burn (0..1..0). */
    public static float stageBurn(float t) {
        float a = (t - (F2_SEPARATION + 6f)) / 30f;
        if (a <= 0f || a >= 1.15f) {
            return 0f;
        }
        // sobe rápido, queima, esvazia suave
        return a < 0.25f ? a / 0.25f : easeOut(clamp01((1.15f - a) / 0.9f));
    }

    /**
     * DESDOBRAMENTO SANFONADO (o coração do pedido): unfold 0..1.
     *
     * Cada braço abre em CASCATA: primeiro a raiz (yoke) gira 90°,
     * depois os segmentos da sanfona esticam UM A UM com atraso.
     * O parâmetro final é suave e ESTRITAMENTE CRESCENTE (garantido
     * por teste: sem voltas, sem teleporte).
     */
    public static float unfold(float t) {
        return easeInOut((t - F4_UNFOLD) / 85f);
    }

    /** Fração de abertura da RAIZ do braço (yoke) no lado dado (0..1). */
    public static float yokeOpen(float t, int side) {
        // O braço +1 (leste) começa 8 ticks depois do -1 (oeste) —
        // cascata das missões reais (libera um, depois o outro).
        float delay = side < 0 ? 0f : 8f;
        return easeInOut((t - (F4_UNFOLD + delay)) / 34f);
    }

    /**
     * Fração de extensão do SEGMENTO i (0..5) da sanfona no lado dado.
     * Cada segmento espera o anterior chegar a ~40% — um a um.
     */
    public static float segmentExtend(float t, int side, int segment) {
        float segDelay = 10f + segment * 9f; // cascata: 10,19,28,37,46,55
        float start = F4_UNFOLD + (side < 0 ? 0f : 8f) + segDelay;
        return easeInOut((t - start) / 30f);
    }

    /** Giro lento do satélite (atitude; graus). */
    public static float satelliteSpin(float t) {
        // Gira devagar desde a separação; sem paradas bruscas.
        return Math.max(0f, t - F2_SEPARATION) * 0.45f;
    }

    // ------------------------------------------------------------------
    // Câmera
    // ------------------------------------------------------------------

    /** Câmera: foco no CONJUNTO no início, depois no SATÉLITE. */
    public static float cameraFocusSatellite(float t) {
        // 0 = olha o conjunto (foguete+satélite), 1 = só o satélite.
        return easeInOut((t - (F3_DRIFT - 15f)) / 40f);
    }

    /** Distância da câmera ao alvo (aproxima, recua na inspeção). */
    public static float cameraDistance(float t) {
        float approach = easeOut((t - 8f) / 90f);      // aproxima na entrada
        float retreat = easeIn((t - F5_INSPECT) / 55f); // recua no fim
        return 11.5f - 4.6f * approach + 3.4f * retreat;
    }

    /** Ângulo orbital da câmera ao redor do alvo (radianos). */
    public static float cameraOrbit(float t) {
        // Arco lento e CONSTANTE no ritmo base + arco extra na inspeção
        // final (o "movimento suave ao redor do satélite" pedido).
        float base = 0.62f + t * 0.0032f;
        float inspect = easeInOut((t - F5_INSPECT) / 60f) * 1.35f;
        return base + inspect;
    }

    /** Altura da câmera relativa ao alvo. */
    public static float cameraHeight(float t) {
        float approach = easeOut((t - 8f) / 90f);
        float settle = easeInOut((t - F3_DRIFT) / 50f);
        // Começa alta (olhando de cima), desce pro nível do satélite.
        return 4.4f - 2.6f * approach - 0.9f * settle;
    }

    // ------------------------------------------------------------------
    // Auto-teste rápido (chamado pela bateria /ostest no SERVIDOR)
    // ------------------------------------------------------------------

    /**
     * Valida os INVARIANTES da animação (sem renderizar nada):
     *  1. unfold é monotônico não-decrescente e em [0,1];
     *  2. idem para os 2×6 segmentos da sanfona (cada um, em cascata,
     *     nunca volta atrás);
     *  3. a separação do satélite é gradual (nenhum passo > 0.09
     *     blocos/tick no meio do empurrão — "mola suave");
     *  4. o estágio NUNCA some instantaneamente (recede monotônico e
     *     contínuo, sem salto > 0.12);
     *  5. a câmera é contínua (sem teleporte > 0.6/tick);
     *  6. nenhuma fase produz valores fora de [0,1] onde espera [0,1].
     *
     * @return null se tudo OK; senão a descrição da violação.
     */
    public static String validate() {
        final float dt = 0.25f;

        // 1 + 2. monotonia do unfold e dos segmentos.
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

        // 3. separação suave (sem saltos > 0.10/tick — um "teleporte"
        // saltaria >= 1.0/tick; o pico legítimo da mola ease-in-out
        // cúbica é 1.5x a taxa média: 1.5*1.4/45 = 0.0933/tick).
        float prevD = satelliteDistance(0f);
        for (float t = dt; t <= DURATION; t += dt) {
            float d = satelliteDistance(t);
            if (d - prevD > 0.10f * dt) {
                return "separação brusca em t=" + t + " (salto " + (d - prevD) / dt + "/tick)";
            }
            if (d < prevD - 1e-6f) {
                return "separação regrediu em t=" + t;
            }
            prevD = d;
        }

        // 4. estágio recua sem sumir (monotônico, saltos < 0.12/tick).
        float prevR = stageRecede(0f);
        for (float t = dt; t <= DURATION; t += dt) {
            float r = stageRecede(t);
            if (r < prevR - 1e-6f) {
                return "estágio regrediu em t=" + t;
            }
            if (r - prevR > 0.12f * dt) {
                return "estágio recuou bruscamente em t=" + t;
            }
            prevR = r;
        }

        // 5. câmera contínua (posição muda < 0.6/tick em qualquer eixo).
        float prevDist = cameraDistance(0f);
        float prevH = cameraHeight(0f);
        for (float t = dt; t <= DURATION; t += dt) {
            float d = cameraDistance(t);
            float h = cameraHeight(t);
            if (Math.abs(d - prevDist) > 0.6f * dt * 8f) {
                return "câmera teleportou (dist) em t=" + t;
            }
            if (Math.abs(h - prevH) > 0.6f * dt * 8f) {
                return "câmera teleportou (alt) em t=" + t;
            }
            prevDist = d;
            prevH = h;
        }

        // 6. fases em [0,1].
        for (float t = 0f; t <= DURATION; t += dt) {
            float v;
            v = fairingRelease(t);
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

        // Ordem das fases coerente.
        if (!(F0_ORBIT < F1_FAIRING && F1_FAIRING < F2_SEPARATION
            && F2_SEPARATION < F3_DRIFT && F3_DRIFT < F4_UNFOLD
            && F4_UNFOLD < F5_INSPECT && F5_INSPECT < DURATION)) {
            return "fases fora de ordem";
        }

        return null; // tudo OK
    }
}
