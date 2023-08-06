package io.github.gaming32.lifesteal.ext;

public interface ServerPlayerExt {
    int ls$getLivesGain();

    void ls$setLivesGain(int gain);

    void ls$refreshLivesGain(int oldValue);
}
