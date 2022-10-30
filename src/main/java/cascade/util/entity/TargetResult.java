package cascade.util.entity;


import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class TargetResult {
    private List<BlockPos> targets = new ArrayList<BlockPos>();
    private boolean valid = true;

    public List<BlockPos> getTargets() {
        return this.targets;
    }

    public TargetResult setTargets(List<BlockPos> targets) {
        this.targets = targets;
        return this;
    }

    public boolean isValid() {
        return this.valid;
    }

    public TargetResult setValid(boolean valid) {
        this.valid = valid;
        return this;
    }
}

