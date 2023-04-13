package org.icst;

import java.io.Serializable;

public interface MiningStrategy extends Serializable {
    long nextNonce(long currentNonce);
}
