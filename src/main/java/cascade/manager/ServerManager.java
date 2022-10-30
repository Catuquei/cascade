package cascade.manager;

import cascade.util.misc.Timer;
import cascade.features.Feature;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Objects;

public class ServerManager extends Feature {

    float[] tpsCounts = new float[10];
    DecimalFormat format = new DecimalFormat("##.00#");
    Timer timer = new Timer();
    float TPS = 20.0f;
    long lastUpdate = -1L;
    String serverBrand = "";

    public void onPacketReceived() {
        timer.reset();
    }

    public boolean isServerNotResponding(int time) {
        return timer.passedMs(time) && !mc.isSingleplayer();
    }

    public long serverRespondingTime() {
        return this.timer.getPassedTimeMs();
    }

    public void update() {
        float tps;
        long currentTime = System.currentTimeMillis();
        if (lastUpdate == -1L) {
            lastUpdate = currentTime;
            return;
        }
        long timeDiff = currentTime - lastUpdate;
        float tickTime = (float) timeDiff / 20.0f;
        if (tickTime == 0.0f) {
            tickTime = 50.0f;
        }
        if ((tps = 1000.0f / tickTime) > 20.0f) {
            tps = 20.0f;
        }
        System.arraycopy(tpsCounts, 0, tpsCounts, 1, tpsCounts.length - 1);
        tpsCounts[0] = tps;
        double total = 0.0;
        for (float f : tpsCounts) {
            total += f;
        }
        if ((total /= tpsCounts.length) > 20.0) {
            total = 20.0;
        }
        TPS = Float.parseFloat(format.format(total));
        lastUpdate = currentTime;
    }

    @Override
    public void reset() {
        Arrays.fill(tpsCounts, 20.0f);
        TPS = 20.0f;
    }

    public float getTpsFactor() {
        return 20.0f / TPS;
    }

    public float getReverseTPSFactor() {
        return TPS / 20.0f;
    }

    public float getTPS() {
        return TPS;
    }

    public String getServerBrand() {
        return serverBrand;
    }

    public void setServerBrand(String brand) {
        serverBrand = brand;
    }

    public int getPing() {
        if (fullNullCheck()) {
            return 0;
        }
        try {
            return Objects.requireNonNull(mc.getConnection()).getPlayerInfo(mc.getConnection().getGameProfile().getId()).getResponseTime();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public float getLagFactor() {
        float lagFactor = getPing() / getTpsFactor();
        return lagFactor;
    }
}