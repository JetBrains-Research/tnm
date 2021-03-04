package util.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.concurrent.ConcurrentSkipListSet

class ConcurrentSkipListSetSerializer<V>(
    private val valueSerializer: KSerializer<V>
) : KSerializer<ConcurrentSkipListSet<V>> {
    override fun serialize(encoder: Encoder, value: ConcurrentSkipListSet<V>) {
        val serializer = SetSerializer(valueSerializer)
        serializer.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): ConcurrentSkipListSet<V> {
        val serializer = SetSerializer(valueSerializer)
        return ConcurrentSkipListSet(serializer.deserialize(decoder))
    }

    override val descriptor get() = SetSerializer(valueSerializer).descriptor

}
