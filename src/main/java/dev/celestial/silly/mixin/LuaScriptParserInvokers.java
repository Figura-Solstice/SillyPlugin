package dev.celestial.silly.mixin;

import org.figuramc.figura.parsers.LuaScriptParser;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LuaScriptParser.class)
public interface LuaScriptParserInvokers {
    @Invoker("noMinifier")
    public String invokeNoMinifier(String script);
    @Invoker("regexMinify")
    public String invokeRegexMinify(String name, String script);
    @Invoker("aggressiveMinify")
    public String invokeAggressiveMinify(String name, String script);
    @Invoker("ASTMinify")
    public String invokeASTMinify(String name, String script);
}
