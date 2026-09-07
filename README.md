# Noveris Item Restrictor

Mod administrativo para Minecraft Java Edition 1.21.1 com NeoForge 21.1.248.

## Build

Requer Java 21. Execute:

```bash
gradle build
```

O JAR estará em `build/libs/`.

## Uso

O comando `/itemrestrict open` abre a interface administrativa e exige nível 2 por padrão.

Comandos de emergência:

```text
/itemrestrict item block <namespace:item>
/itemrestrict item allow <namespace:item> <player>
/itemrestrict mod block <namespace>
/itemrestrict mod unblock <namespace>
/itemrestrict list
```

As regras são salvas como `SavedData` no mundo server-side e identificam jogadores por UUID. A decisão de posse, uso, interação e craft é tomada no servidor; o cliente só desenha a tela e envia intenções que são validadas novamente.

## Arquitetura

- `RestrictionManager`: única autoridade de decisão.
- `RestrictionData`: persistência, allowlists, mods restritos e auditoria.
- `RestrictionEvents`: bloqueios de interação, combate, uso, pickup, craft e varredura periódica.
- `NetworkHandler`: payloads registrados no NeoForge e validação de permissão no servidor.
- `RestrictionAdminScreen`: painel visual responsivo com paleta Noveris.

## Nota de implantação

Instale no servidor dedicado e nos clientes administrativos. O servidor deve ser o responsável pela configuração; nenhum pacote enviado pelo cliente é aceito sem validação de nível de permissão.
