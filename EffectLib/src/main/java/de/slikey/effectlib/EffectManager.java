package de.slikey.effectlib;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.awt.Font;
import java.util.List;
import java.util.UUID;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;

import org.bukkit.Color;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.util.Vector;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.ConfigurationSection;

import com.google.common.base.CaseFormat;

import de.slikey.effectlib.util.*;

/**
 * Dispose the EffectManager if you don't need it anymore.
 *
 * @author Kevin
 *
 */
public class EffectManager implements Disposable, ParticleEffectManager {

    private static final List<EffectManager> effectManagers = new ArrayList<>();
    private static final Map<String, Class<? extends Effect>> effectClasses = new HashMap<>();
    private Plugin owningPlugin;
    private Logger logger;
    private Map<Effect, BukkitTask> effects;
    private ParticleDisplay display;
    private boolean disposed;
    private boolean disposeOnTermination;
    private boolean debug = false;
    private boolean stackTraces = true;
    private int visibleRange = 32;
    private boolean forceShow = false;
    private File imageCacheFolder;
    private Map<String, BufferedImage[]> imageCache;
    private final Set<UUID> ignoredPlayers = new HashSet<>();

    public EffectManager(Plugin owningPlugin) {
        this(owningPlugin, owningPlugin.getLogger());
    }

    public EffectManager(Plugin owningPlugin, Logger logger) {
        if (owningPlugin == null) {
            throw new IllegalArgumentException("EffectManager must be given a valid owning plugin");
        }
        this.owningPlugin = owningPlugin;
        this.logger = logger;

        imageCacheFolder = new File(owningPlugin.getDataFolder(), "imagecache");
        imageCache = new HashMap<>();
        effects = new HashMap<>();
        disposed = false;
        disposeOnTermination = false;
        effectManagers.add(this);
    }

    public static List<EffectManager> getManagers() {
        return effectManagers;
    }

    public void display(Particle particle, Location center, float offsetX, float offsetY, float offsetZ, float speed, int amount, float size, Color color, Material material, byte materialData, double range, List<Player> targetPlayers) {
        ParticleOptions options = new ParticleOptions(offsetX, offsetY, offsetZ, speed, amount, size, color, material, materialData);
        getDisplay().display(particle, options, center, range, targetPlayers);
    }

    public void display(Particle particle, ParticleOptions options, Location center, double range, List<Player> targetPlayers) {
        getDisplay().display(particle, options, center, range, targetPlayers);
    }

    private ParticleDisplay getDisplay() {
        if (display == null) {
            display = ParticleDisplayFactory.newInstance();
        }

        display.setManager(this);

        return display;
    }

    public Effect start(String effectClass, ConfigurationSection parameters, Location origin, Entity originEntity) {
        return start(effectClass, parameters, origin, null, originEntity, null, null);
    }

    public Effect start(String effectClass, ConfigurationSection parameters, Entity originEntity) {
        return start(effectClass, parameters, originEntity == null ? null : originEntity.getLocation(), null, originEntity, null, null);
    }

    public Effect start(String effectClass, ConfigurationSection parameters, Location origin) {
        return start(effectClass, parameters, origin, null, null, null, null);
    }

    public Effect start(String effectClass, ConfigurationSection parameters, Location origin, Player targetPlayer){
        return start(effectClass, parameters, new DynamicLocation(origin, null), new DynamicLocation(null, null), (ConfigurationSection)null, targetPlayer);
    }

    /**
     * Start an Effect from a Configuration map of parameters.
     *
     * @param effectClass The name of the Effect class to instantiate. If unqualified, defaults to the de.slikey.effectlib.effect namespace.
     * @param parameters A Configuration-driven map of key/value parameters. Each of these will be applied directly to the corresponding field in the Effect instance.
     * @param origin The origin Location
     * @param target The target Location, only used in some Effects (like LineEffect)
     * @param originEntity The origin Entity, the effect will attach to the Entity's Location
     * @param targetEntity The target Entity, only used in some Effects
     * @param parameterMap A map of parameter values to replace. These must start with the "$" character, values in the parameters map that contain a $key will be replaced with the value in this parameterMap.
     * @return effect or null
     */
    @Deprecated
    public Effect start(String effectClass, ConfigurationSection parameters, Location origin, Location target, Entity originEntity, Entity targetEntity, Map<String, String> parameterMap) {
        return start(effectClass, parameters, new DynamicLocation(origin, originEntity), new DynamicLocation(target, targetEntity), parameterMap);
    }

    /**
     * Start an Effect from a Configuration map of parameters.
     *
     * @param effectClass The name of the Effect class to instantiate. If unqualified, defaults to the de.slikey.effectlib.effect namespace.
     * @param parameters A Configuration-driven map of key/value parameters. Each of these will be applied directly to the corresponding field in the Effect instance.
     * @param origin The origin Location
     * @param target The target Location, only used in some Effects (like LineEffect)
     * @param parameterMap A map of parameter values to replace. These must start with the "$" character, values in the parameters map that contain a $key will be replaced with the value in this parameterMap.
     * @return effect or null
     */
    @Deprecated
    public Effect start(String effectClass, ConfigurationSection parameters, DynamicLocation origin, DynamicLocation target, Map<String, String> parameterMap) {
        return start(effectClass, parameters, origin, target, parameterMap, null);
    }

    public Effect getEffectByClassName(String effectClass) {
        Class<? extends Effect> effectLibClass;
        try {
            // First check the name as given
            effectLibClass = effectClasses.get(effectClass);

            // A shaded manager may provide a fully-qualified path.
            if (effectLibClass == null && !effectClass.contains(".")) {
                effectClass = "de.slikey.effectlib.effect." + effectClass;
                if (!effectClass.endsWith("Effect")) effectClass = effectClass + "Effect";
                effectLibClass = effectClasses.get(effectClass);
            }
            if (effectLibClass == null) {
                effectLibClass = (Class<? extends Effect>) Class.forName(effectClass);
                effectClasses.put(effectClass, effectLibClass);
            }
        } catch (Throwable ex) {
            onError("Error loading EffectLib class: " + effectClass, ex);
            return null;
        }

        Effect effect = null;
        try {
            Constructor<? extends Effect> constructor = effectLibClass.getConstructor(EffectManager.class);
            effect = (Effect) constructor.newInstance(this);
        } catch (Exception ex) {
            onError("Error loading EffectLib class: " + effectClass, ex);
        }

        return effect;
    }

    public void start(Effect effect) {
        if (disposed) throw new IllegalStateException("EffectManager is disposed and not able to accept any effects.");
        if (disposeOnTermination) throw new IllegalStateException("EffectManager is awaiting termination to dispose and not able to accept any effects.");
        if (effects.containsKey(effect)) effect.cancel(false);
        if (!owningPlugin.isEnabled()) return;

        BukkitScheduler s = Bukkit.getScheduler();
        BukkitTask task = null;
        switch (effect.getType()) {
            case INSTANT:
                if (effect.isAsynchronous()) task = s.runTaskAsynchronously(owningPlugin, effect);
                else task = s.runTask(owningPlugin, effect);
                break;
            case DELAYED:
                if (effect.isAsynchronous()) task = s.runTaskLaterAsynchronously(owningPlugin, effect, effect.getDelay());
                else task = s.runTaskLater(owningPlugin, effect, effect.getDelay());
                break;
            case REPEATING:
                if (effect.isAsynchronous()) task = s.runTaskTimerAsynchronously(owningPlugin, effect, effect.getDelay(), effect.getPeriod());
                else task = s.runTaskTimer(owningPlugin, effect, effect.getDelay(), effect.getPeriod());
                break;
        }
        synchronized (this) {
            effect.setStartTime(System.currentTimeMillis());
            effects.put(effect, task);
        }
    }

    public Effect getEffect(String effectClass, ConfigurationSection parameters, DynamicLocation origin, DynamicLocation target, ConfigurationSection parameterMap, Player targetPlayer, String logContext) {
        List<Player> targetPlayers = null;
        if (targetPlayer != null) {
            targetPlayers = new ArrayList<>();
            targetPlayers.add(targetPlayer);
        }
        return getEffect(effectClass, parameters, origin, target, parameterMap, targetPlayers, logContext);
    }

    public Effect getEffect(String effectClass, ConfigurationSection parameters, DynamicLocation origin, DynamicLocation target, ConfigurationSection parameterMap, List<Player> targetPlayers, String logContext) {
        Effect effect = getEffectByClassName(effectClass);
        if (effect == null) return null;

        // Some specific shortcuts
        if (parameters.contains("particle_offset")) {
            parameters.set("particle_offset_x", parameters.get("particle_offset"));
            parameters.set("particle_offset_y", parameters.get("particle_offset"));
            parameters.set("particle_offset_z", parameters.get("particle_offset"));
            parameters.set("particle_offset", null);
        }
        if (parameters.contains("particleOffset")) {
            parameters.set("particleOffsetX", parameters.get("particleOffset"));
            parameters.set("particleOffsetY", parameters.get("particleOffset"));
            parameters.set("particleOffsetZ", parameters.get("particleOffset"));
            parameters.set("particleOffset", null);
        }

        Collection<String> keys = parameters.getKeys(false);
        for (String key : keys) {
            if (key.equals("class")) continue;

            setField(effect, key, parameters, parameterMap, logContext);
        }

        effect.initialize();
        if (origin != null) effect.setDynamicOrigin(origin);
        effect.setDynamicTarget(target);

        effect.setTargetPlayers(targetPlayers);

        return effect;
    }

    @Deprecated
    public Effect start(String effectClass, ConfigurationSection parameters, DynamicLocation origin, DynamicLocation target, Map<String, String> parameterMap, Player targetPlayer) {
        ConfigurationSection configMap = null;
        if (parameterMap != null) configMap = ConfigUtils.toStringConfiguration(parameterMap);

        return start(effectClass, parameters, origin, target, configMap, targetPlayer);
    }

    /**
     * Start an effect, possibly using parameter replacement.
     *
     * @param effectClass the effect class to start
     * @param parameters any parameters to pass to the effect
     * @param origin the origin location
     * @param target the target location
     * @param parameterMap a configuration of variables from the parameter config to replace
     * @param targetPlayer The player who should see this effect.
     * @return effect or null
     */
    public Effect start(String effectClass, ConfigurationSection parameters, DynamicLocation origin, DynamicLocation target, ConfigurationSection parameterMap, Player targetPlayer) {
        Effect effect = getEffect(effectClass, parameters, origin, target, parameterMap, targetPlayer);
        if (effect == null) return null;
        effect.start();
        return effect;
    }
    
    public void cancel(boolean callback) {
        synchronized (this) {
            if (effects == null) return;
            List<Effect> allEffects = new ArrayList<>(effects.keySet());
            for (Effect effect : allEffects) {
                effect.cancel(callback);
            }
        }
    }

    public void done(Effect effect) {
        synchronized (this) {
            removeEffect(effect);
            if (effect.callback != null && owningPlugin.isEnabled()) Bukkit.getScheduler().runTask(owningPlugin, effect.callback);
            if (disposeOnTermination && effects != null && effects.isEmpty()) dispose();
        }
    }

    public void removeEffect(Effect effect) {
        synchronized (this) {
            if (effects == null) return;
            BukkitTask existingTask = effects.get(effect);
            if (existingTask != null) existingTask.cancel();
            effects.remove(effect);
        }
    }

    @Override
    public void dispose() {
        synchronized (this) {
            if (disposed) return;
            disposed = true;
            cancel(false);
            effects = null;
            owningPlugin = null;
            logger = null;
            display = null;
            imageCache = null;
            imageCacheFolder = null;
            effectManagers.remove(this);
        }
    }

    public void disposeOnTermination() {
        synchronized (this) {
            disposeOnTermination = true;
            if (effects != null && effects.isEmpty()) dispose();
        }
    }

    public boolean isDisposed() {
        return disposed;
    }

    public boolean isDisposedOnTermination() {
        return disposeOnTermination;
    }

    public void enableDebug(boolean enable) {
        debug = enable;
    }

    public void enableStackTraces(boolean enable) {
        stackTraces = enable;
    }

    public boolean isDebugEnabled() {
        return debug;
    }

    public void onError(Throwable ex) {
        getLogger().log(Level.SEVERE, "Unexpected EffectLib Error: " + ex.getMessage(), ex);
    }

    public Effect getEffect(String effectClass, ConfigurationSection parameters, DynamicLocation origin, DynamicLocation target, ConfigurationSection parameterMap, List<Player> targetPlayers) {
        return getEffect(effectClass, parameters, origin, target, parameterMap, targetPlayers, "Unknown");
    }

    public Effect getEffect(String effectClass, ConfigurationSection parameters, DynamicLocation origin, DynamicLocation target, ConfigurationSection parameterMap, Player targetPlayer) {
        return getEffect(effectClass, parameters, origin, target, parameterMap, targetPlayer, "Unknown");
    }

    public void onError(String message, Throwable ex) {
        if (!debug) return;
        if (stackTraces) getLogger().log(Level.WARNING, message, ex);
        else getLogger().log(Level.WARNING, message);
    }

    public Logger getLogger() {
        return logger;
    }

    public Map<Effect, BukkitTask> getEffects() {
        return effects;
    }

    public int getParticleRange() {
        return visibleRange;
    }

    public void setParticleRange(int range) {
        visibleRange = range;
    }

    public boolean getForceShow() {
        return forceShow;
    }

    public void setForceShow(boolean force) {
        forceShow = force;
    }

    public Plugin getOwningPlugin() {
        return owningPlugin;
    }

    public void onError(String message) {
        if (debug) getLogger().log(Level.WARNING, message);
    }

    protected boolean setField(Object effect, String key, ConfigurationSection section, ConfigurationSection parameterMap, String logContext) {
        try {
            logContext = logContext == null ? "(?)" : logContext;
            String fieldKey = key;
            String stringValue = section.getString(key);
            if (stringValue == null) {
                onError("Null value for EffectLib property " + key + " of class " + effect.getClass().getSimpleName() + " in " + logContext);
                return false;
            }

            // Allow underscore_style and dash_style parameters
            if (key.contains("-")) key = key.replace("-", "_");

            if (key.contains("_")) key = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, key);

            ConfigurationSection fieldSection = section;
            if (parameterMap != null && stringValue.startsWith("$") && parameterMap.contains(stringValue)) {
                fieldKey = stringValue;
                fieldSection = parameterMap;
            }

            Field field = effect.getClass().getField(key);

            if (field.getType().equals(Integer.TYPE) || field.getType().equals(Integer.class)) {
                int intValue = Integer.MAX_VALUE;
                if (!ConfigUtils.isMaxValue(stringValue)) intValue = fieldSection.getInt(fieldKey);
                field.set(effect, intValue);
            } else if (field.getType().equals(Float.TYPE) || field.getType().equals(Float.class)) {
                float floatValue = Float.MAX_VALUE;
                if (!ConfigUtils.isMaxValue(stringValue)) floatValue = (float) fieldSection.getDouble(fieldKey);
                field.set(effect,floatValue);
            } else if (field.getType().equals(Double.TYPE) || field.getType().equals(Double.class)) {
                double doubleValue = Double.MAX_VALUE;
                if (!ConfigUtils.isMaxValue(stringValue)) doubleValue = fieldSection.getDouble(fieldKey);
                field.set(effect, doubleValue);
            } else if (field.getType().equals(Boolean.TYPE) || field.getType().equals(Boolean.class)) {
                field.set(effect, fieldSection.getBoolean(fieldKey));
            } else if (field.getType().equals(Long.TYPE) || field.getType().equals(Long.class)) {
                long longValue = Long.MAX_VALUE;
                if (!ConfigUtils.isMaxValue(stringValue)) longValue = fieldSection.getLong(fieldKey);
                field.set(effect, longValue);
            } else if (field.getType().equals(Short.TYPE) || field.getType().equals(Short.class)) {
                short shortValue = Short.MAX_VALUE;
                if (!ConfigUtils.isMaxValue(stringValue)) shortValue = (short) fieldSection.getInt(fieldKey);
                field.set(effect, shortValue);
            } else if (field.getType().equals(Byte.TYPE) || field.getType().equals(Byte.class)) {
                byte byteValue = Byte.MAX_VALUE;
                if (!ConfigUtils.isMaxValue(stringValue)) byteValue = (byte) fieldSection.getInt(fieldKey);
                field.set(effect, byteValue);
            } else if (field.getType().equals(String.class)) {
                String value = fieldSection.getString(fieldKey);
                field.set(effect, value);
            } else if (field.getType().equals(Color.class)) {
                String value = fieldSection.getString(fieldKey);
                if (value != null) {
                    int rgb;
                    if (value.equalsIgnoreCase("random")) {
                        byte red = (byte) (Math.random() * 255);
                        byte green = (byte) (Math.random() * 255);
                        byte blue = (byte) (Math.random() * 255);
                        rgb = (red << 16) | (green << 8) | blue;
                    } else {
                        if (value.startsWith("#")) value = value.substring(1);
                        rgb = Integer.parseInt(value, 16);
                    }
                    field.set(effect, Color.fromRGB(rgb));
                }
            } else if (Map.class.isAssignableFrom(field.getType()) && section.isConfigurationSection(key)) {
                Map<String, Object> map = (Map<String, Object>) field.get(effect);
                ConfigurationSection subSection = section.getConfigurationSection(key);
                if (subSection != null) {
                    Set<String> keys = subSection.getKeys(false);
                    for (String mapKey : keys) {
                        map.put(mapKey, subSection.get(mapKey));
                    }
                }
            } else if (Map.class.isAssignableFrom(field.getType()) && Map.class.isAssignableFrom(section.get(key).getClass())) {
                field.set(effect, section.get(key));
            } else if (ConfigurationSection.class.isAssignableFrom(field.getType())) {
                ConfigurationSection configSection = ConfigUtils.getConfigurationSection(section, key);
                if (parameterMap != null) {
                    ConfigurationSection baseConfiguration = configSection;
                    configSection = new MemoryConfiguration();
                    if (baseConfiguration != null) {
                        Set<String> keys = baseConfiguration.getKeys(false);
                        // Note this doesn't handle sections within sections.
                        for (String baseKey : keys) {
                            Object baseValue = baseConfiguration.get(baseKey);
                            if (baseValue instanceof String && ((String) baseValue).startsWith("$")) {
                                // If this is an equation it will get parsed when needed
                                String parameterValue = parameterMap.getString((String) baseValue);
                                baseValue = parameterValue == null ? baseValue : parameterValue;
                            }
                            configSection.set(baseKey, baseValue);
                        }
                    }
                }
                field.set(effect, configSection);
            } else if (field.getType().equals(Vector.class)) {
                String value = fieldSection.getString(fieldKey);
                if (value != null) {
                    String[] pieces = value.split(",");
                    double x = pieces.length > 0 ? Double.parseDouble(pieces[0]) : 0;
                    double y = pieces.length > 1 ? Double.parseDouble(pieces[1]) : 0;
                    double z = pieces.length > 2 ? Double.parseDouble(pieces[2]) : 0;
                    field.set(effect, new Vector(x, y, z));
                }
            } else if (field.getType().equals(Particle.class)) {
                String value = fieldSection.getString(fieldKey);
                if (value != null) {
                    // Legacy conversions
                    if (!getDisplay().hasColorTransition() && value.equalsIgnoreCase("DUST_COLOR_TRANSITION")) {
                        value = "REDSTONE";
                    }
                    field.set(effect, ParticleUtil.getParticle(value));
                }
            } else if (field.getType().isEnum()) {
                Class<Enum> enumType = (Class<Enum>) field.getType();
                String value = fieldSection.getString(fieldKey);
                if (value != null) {
                    field.set(effect, Enum.valueOf(enumType, value.toUpperCase()));
                }
            } else if (field.getType().equals(Font.class)) {
                // Should caching the fonts be considered?
                // Or is the performance gain negligible?
                String value = fieldSection.getString(fieldKey);
                field.set(effect, Font.decode(value));
            } else if (field.getType().equals(CustomSound.class)) {
                String value = fieldSection.getString(fieldKey);
                field.set(effect, new CustomSound(value));
            } else {
                onError("Unable to assign EffectLib property " + key + " of class " + effect.getClass().getSimpleName() + " in " + logContext);
                return false;
            }

            return true;
        } catch (Exception ex) {
            onError("Error assigning EffectLib property: '" + key + "' of class: '" + effect.getClass().getSimpleName() + "' in: '" + logContext + "': " + ex.getMessage(), ex);
        }

        return false;
    }

    public static void disposeAll() {
        Iterator<EffectManager> i = effectManagers.iterator();
        while (i.hasNext()) {
            EffectManager em = i.next();
            i.remove();
            em.dispose();
        }
    }

    public void setImageCacheFolder(File folder) {
        imageCacheFolder = folder;
    }

    public File getImageCacheFolder() {
        return imageCacheFolder;
    }

    public void loadImage(final String fileName, final ImageLoadCallback callback) {
        BufferedImage[] images = imageCache.get(fileName);
        if (images != null) {
            callback.loaded(images);
            return;
        }

        owningPlugin.getServer().getScheduler().runTaskAsynchronously(owningPlugin, new ImageLoadTask(this, fileName, new ImageLoadCallback() {
            @Override
            public void loaded(final BufferedImage[] images) {
                 owningPlugin.getServer().getScheduler().runTask(owningPlugin, new Runnable() {
                     @Override
                     public void run() {
                         imageCache.put(fileName, images);
                         callback.loaded(images);
                     }
                 });
            }
        }));
    }

    public void registerEffectClass(String key, Class<? extends Effect> effectClass) {
        effectClasses.put(key, effectClass);
    }

    public boolean isPlayerIgnored(Player player) {
        return ignoredPlayers.contains(player.getUniqueId());
    }

    public void ignorePlayer(Player player, boolean ignore) {
        if (ignore) ignoredPlayers.add(player.getUniqueId());
        else ignoredPlayers.remove(player.getUniqueId());
    }

    public boolean isVisiblePlayer(Player player, Location center, double distanceSquared) {
        if (isPlayerIgnored(player)) return false;
        if (player.getWorld() != center.getWorld()) return false;
        if (player.getLocation().distanceSquared(center) > distanceSquared) return false;
        return true;
    }

    public List<Player> getVisiblePlayers(Location center, double range) {
        double squared = range * range;
        List<Player> visible = new ArrayList<>();
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (isVisiblePlayer(player, center, squared)) {
                visible.add(player);
            }
        }
        return visible;
    }
}
