package com.example.focus.audio

enum class AmbientSoundType(
    val id: String,
    val displayName: String,
    val description: String,
    val iconName: String,
    val colorHex: Long
) {
    OFF("OFF", "Mute / Off", "No background sound", "volume_off", 0xFF64748B),
    RAIN("RAIN", "Rainfall", "Gentle soothing rain & gentle droplets", "water_drop", 0xFF38BDF8),
    WHITE_NOISE("WHITE_NOISE", "White Noise", "Calming broadband frequency masking", "air", 0xFFA78BFA),
    OCEAN_WAVES("OCEAN_WAVES", "Ocean Waves", "Rhythmic rolling surf & sea foam", "waves", 0xFF2DD4BF),
    CAMPFIRE("CAMPFIRE", "Campfire", "Cozy fireplace warmth & gentle crackles", "local_fire_department", 0xFFFB923C),
    BINAURAL_ALPHA("BINAURAL_ALPHA", "Alpha Waves (10Hz)", "Binaural pulse for deep focus & flow", "graphic_eq", 0xFF818CF8),
    FOREST_BREEZE("FOREST_BREEZE", "Forest Breeze", "Gentle woodland wind & distant birds", "park", 0xFF4ADE80),
    COFFEE_SHOP("COFFEE_SHOP", "Cozy Murmur", "Warm background acoustic ambiance", "coffee", 0xFFFBBF24);

    companion object {
        fun fromId(id: String): AmbientSoundType {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: OFF
        }
    }
}

data class AmbientPlaybackState(
    val soundType: AmbientSoundType = AmbientSoundType.OFF,
    val isPlaying: Boolean = false,
    val volume: Float = 0.7f,
    val isLinkedToTimer: Boolean = true
)
