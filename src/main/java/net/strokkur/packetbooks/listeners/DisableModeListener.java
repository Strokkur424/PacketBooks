package net.strokkur.packetbooks.listeners;

import net.strokkur.packetbooks.PacketBooks;

public class DisableModeListener extends AbstractModeListener {

  public DisableModeListener(final PacketBooks plugin) {
    super(plugin);
  }

  @Override
  public String getName() {
    return "disabled";
  }
}
