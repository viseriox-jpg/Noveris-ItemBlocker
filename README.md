# Noveris Item Restrictor

Sistema server-side para Minecraft 1.21.1 / NeoForge 21.1.248 / Java 21.

## Instalação

Use Java 21, execute `./gradlew build` e copie `build/libs/noveris_item_restrictor-*.jar` para `mods/` do servidor dedicado.

O estado fica exclusivamente no `SavedData` do Overworld. O servidor é sempre a autoridade final; a GUI apenas envia intenções.

## Comandos

```text
/itemrestrict open
/itemrestrict item block <item>
/itemrestrict item unblock <item>
/itemrestrict item allow <item> <uuid>
/itemrestrict item deny <item> <uuid>
/itemrestrict item remove <item>
/itemrestrict mod block <modid>
/itemrestrict mod unblock <modid>
/itemrestrict list
/itemrestrict reload
/itemrestrict audit
```

Os comandos exigem nível 2 por padrão. Jogadores são identificados por UUID.

## Segurança e limitações

Regras, allowlists, namespaces, jogadores conhecidos, configuração e auditoria são persistidos com recuperação segura de dados inválidos. Uso, posse, craft, equipamento, transferência e interações são centralizados no `RestrictionManager`; a varredura periódica remove itens proibidos e os solta no mundo. Mods que entreguem itens por canais próprios podem exigir integração adicional.

O workflow do GitHub Actions executa `./gradlew build` com Java 21 e publica o JAR como artefato.
