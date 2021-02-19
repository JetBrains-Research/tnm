package util.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.concurrent.ConcurrentHashMap

class ConcurrentHashMapSerializer<K, V>(
    private val keySerializer: KSerializer<K>,
    private val valueSerializer: KSerializer<V>
): KSerializer<ConcurrentHashMap<K, V>> {
    override fun serialize(encoder: Encoder, value: ConcurrentHashMap<K, V>) {
        val serializer = MapSerializer(keySerializer, valueSerializer)
        serializer.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): ConcurrentHashMap<K, V> {
        val serializer = MapSerializer(keySerializer, valueSerializer)
        return ConcurrentHashMap(serializer.deserialize(decoder))
    }
    override val descriptor get() = MapSerializer(keySerializer, valueSerializer).descriptor
}
