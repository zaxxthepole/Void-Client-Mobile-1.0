package org.cloudburstmc.protocol.bedrock.data.skin;

import lombok.Data;

import java.util.UUID;

@Data
public class PersonaPieceData {

    String id;
    PersonaPieceType pieceType;
    UUID packUuid;
    boolean isDefault;
    String productId;

    public PersonaPieceData(String id,
                            String type,
                            String packId,
                            boolean isDefault,
                            String productId) {
        this.id = id;
        this.pieceType = PersonaPieceType.fromName(type);
        this.packUuid = UUID.fromString(packId);
        this.isDefault = isDefault;
        this.productId = productId;
    }

    public PersonaPieceData(String id,
                            PersonaPieceType pieceType,
                            UUID packId,
                            boolean isDefault,
                            String productId) {
        this.id = id;
        this.pieceType = pieceType;
        this.packUuid = packId;
        this.isDefault = isDefault;
        this.productId = productId;
    }

    public String getPackId() {
        return packUuid.toString();
    }

    public String getType() {
        return pieceType.getSerializeName();
    }
}
