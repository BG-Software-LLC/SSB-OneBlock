package com.bgsoftware.ssboneblock.nms;

import com.bgsoftware.common.nmsloader.config.NMSConfiguration;
import com.bgsoftware.ssboneblock.OneBlockModule;

import java.io.File;

public class ModuleNMSConfiguration extends NMSConfiguration {

    private final File cacheFolder;

    public ModuleNMSConfiguration(OneBlockModule module) {
        this.cacheFolder = new File(module.getModuleFolder(), ".cache");
    }

    @Override
    public String getNMSResourcePathForVersion(String nmsVersionName) {
        return String.format("com/bgsoftware/ssboneblock/nms/%s", nmsVersionName);
    }

    @Override
    public String getPackagePathForNMSHandler(String nmsVersionName, String handlerName) {
        return String.format("com.bgsoftware.ssboneblock.nms.%s.%s", nmsVersionName, handlerName + "Impl");
    }

    @Override
    public File getCacheFolder() {
        return this.cacheFolder;
    }

}
