package ru.kod.client.modules.visuals;

import ru.kod.client.modules.Module;

/** NoHurtCam: removes the camera shake on damage (handled in mixin, see KodClient). */
public class NoHurtCam extends Module {
    public NoHurtCam() { super("NoHurtCam", "Visuals"); }
}
