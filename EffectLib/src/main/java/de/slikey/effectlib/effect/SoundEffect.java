package de.slikey.effectlib.effect;

import de.slikey.effectlib.Effect;
import de.slikey.effectlib.EffectType;
import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.util.CustomSound;

public class SoundEffect extends Effect {

    /**
     * Sound effect to play
     * Format: <soundName>,<volume>,<pitch>,<range>
     */
    public CustomSound sound;

    public SoundEffect(EffectManager effectManager) {
        super(effectManager);
        type = EffectType.REPEATING;
        period = 1;
        iterations = 1;
    }

    @Override
    public void onRun() {
        if (sound == null) return;
        sound.play(effectManager.getOwningPlugin(), getLocation());
    }

}
