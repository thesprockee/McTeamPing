package io.sprock.teamping.client;

import java.awt.Color;
import java.sql.Timestamp;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.BlockPos;

public class Marker {

	public static enum Symbol {
		HERE, NOTICE, QUESTION, NO, YES, DEFEND, ATTACK, MINE
	}

	private Symbol symbol;

	private BlockPos blockPos;
	private Timestamp timestamp = new Timestamp(System.currentTimeMillis());

	private static EnumMap<Symbol, String> encodeMap = new EnumMap<>(Symbol.class);
	private static final Map<String, Symbol> decodeMap = new HashMap<String, Symbol>();
	private static EnumMap<Symbol, Color> colorMap = new EnumMap<>(Symbol.class);

	private static void putMaps(Symbol symbol, String s, Color c) {
		encodeMap.put(symbol, s);
		decodeMap.put(s, symbol);
		colorMap.put(symbol, c);

	}

	static {

		// colors from https://colorswall.com/palette/59048

		putMaps(Symbol.HERE, "x", new Color(249, 255, 254));
		putMaps(Symbol.NOTICE, "n", new Color(254, 216, 61));
		putMaps(Symbol.QUESTION, "q", new Color(249, 128, 29));
		putMaps(Symbol.NO, "N", new Color(157, 157, 151));
		putMaps(Symbol.YES, "Y", new Color(128, 199, 31));
		putMaps(Symbol.DEFEND, "d", new Color(60, 68, 170));
		putMaps(Symbol.ATTACK, "a", new Color(176, 46, 38));
		putMaps(Symbol.MINE, "m", new Color(199, 78, 189));
	}

	public Marker(BlockPos blockPos, Symbol symbol) {

		this.blockPos = blockPos;
		this.symbol = symbol;

		timestamp = new Timestamp(System.currentTimeMillis());
	}

	public static Marker fromData(int x, int y, int z, String symbolCode) {

		return new Marker(new BlockPos(x, y, z), decodeMap.get(symbolCode));
	}

	public BlockPos getBlockPos() {
		return blockPos;
	}

	public static String getCode(Symbol symbol) {
		return encodeMap.get(symbol);
	}

	public String getCode() {
		return getCode(symbol);
	}

	public static String getCode(int index) {
		return getCode(Symbol.values()[index]);
	}

	public Color getColor() {
		return colorMap.get(symbol);
	}

	public Symbol getSymbol() {
		return symbol;
	}

	public int getTextureIndex() {
		return symbol.ordinal();

	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

}
