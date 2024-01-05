package caveworld.network;

import java.util.List;

import caveworld.core.Caveworld;
import caveworld.network.client.BrightnessAdjustMessage;
import caveworld.network.client.CaveAdjustMessage;
import caveworld.network.client.CaveMusicMessage;
import caveworld.network.client.CaverAdjustMessage;
import caveworld.network.client.CaveworldMenuMessage;
import caveworld.network.client.LastMineMessage;
import caveworld.network.client.MultiBreakCountMessage;
import caveworld.network.client.PortalMenuMessage;
import caveworld.network.client.RegenerationGuiMessage;
import caveworld.network.common.HeldItemNBTAdjustMessage;
import caveworld.network.common.OpRemoteCheckMessage;
import caveworld.network.common.VeinAdjustMessage;
import caveworld.network.server.CaveAchievementMessage;
import caveworld.network.server.OpenGuiMessage;
import caveworld.network.server.PortalInventoryMessage;
import caveworld.network.server.RegenerationMessage;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.server.MinecraftServer;

public class CaveNetworkRegistry
{
	public static final SimpleNetworkWrapper network = NetworkRegistry.INSTANCE.newSimpleChannel(Caveworld.MODID);

	public static int messageId;

	public static <REQ extends IMessage, REPLY extends IMessage> void registerMessage(Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Class<REQ> requestMessageType, Side side)
	{
		network.registerMessage(messageHandler, requestMessageType, messageId++, side);
	}

	public static <REQ extends IMessage, REPLY extends IMessage> void registerMessage(Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Class<REQ> requestMessageType)
	{
		registerMessage(messageHandler, requestMessageType, Side.CLIENT);
		registerMessage(messageHandler, requestMessageType, Side.SERVER);
	}

	public static Packet getPacket(IMessage message)
	{
		return network.getPacketFrom(message);
	}

	public static void sendToAll(IMessage message)
	{
		network.sendToAll(message);
	}

	public static void sendToOthers(IMessage message, EntityPlayerMP player)
	{
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();

		if (server != null && server.isDedicatedServer())
		{
			for (EntityPlayerMP thePlayer : (List<EntityPlayerMP>)server.getConfigurationManager().playerEntityList)
			{
				if (player == thePlayer)
				{
					sendTo(message, thePlayer);
				}
			}
		}
	}

	public static void sendTo(IMessage message, EntityPlayerMP player)
	{
		network.sendTo(message, player);
	}

	public static void sendToDimension(IMessage message, int dimensionId)
	{
		network.sendToDimension(message, dimensionId);
	}

	public static void sendToServer(IMessage message)
	{
		network.sendToServer(message);
	}

	public static void registerMessages()
	{
		registerMessage(OpRemoteCheckMessage.class, OpRemoteCheckMessage.class);
		registerMessage(CaveAdjustMessage.class, CaveAdjustMessage.class, Side.CLIENT);
		registerMessage(CaverAdjustMessage.class, CaverAdjustMessage.class, Side.CLIENT);
		registerMessage(VeinAdjustMessage.class, VeinAdjustMessage.class);
		registerMessage(BrightnessAdjustMessage.class, BrightnessAdjustMessage.class, Side.CLIENT);
		registerMessage(CaveMusicMessage.class, CaveMusicMessage.class, Side.CLIENT);
		registerMessage(HeldItemNBTAdjustMessage.class, HeldItemNBTAdjustMessage.class);
		registerMessage(RegenerationGuiMessage.class, RegenerationGuiMessage.class, Side.CLIENT);
		registerMessage(RegenerationMessage.class, RegenerationMessage.class, Side.SERVER);
		registerMessage(CaveworldMenuMessage.class, CaveworldMenuMessage.class, Side.CLIENT);
		registerMessage(PortalMenuMessage.class, PortalMenuMessage.class, Side.CLIENT);
		registerMessage(CaveAchievementMessage.class, CaveAchievementMessage.class, Side.SERVER);
		registerMessage(MultiBreakCountMessage.class, MultiBreakCountMessage.class, Side.CLIENT);
		registerMessage(PortalInventoryMessage.class, PortalInventoryMessage.class, Side.SERVER);
		registerMessage(LastMineMessage.class, LastMineMessage.class, Side.CLIENT);
		registerMessage(OpenGuiMessage.class, OpenGuiMessage.class, Side.SERVER);
	}
}