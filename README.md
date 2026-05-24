# RealEstateClaims

RealEstateClaims is a PaperMC land claim plugin for Minecraft 1.26.1.2, built for Java 21.
It provides buyable real estate claims, Vault economy integration, sign purchase flow, claim protections, trusted players, and GUI support.

## Features

- Admin land selection wand (`/lcwand`)
- Land claims created by selection and sign placement
- Auto-incrementing claim IDs
- Vault purchase system with configurable price
- Sign-based purchase and claim info interaction
- Claim protection for blocks, containers, redstone, entities, explosions, PvP, and movement
- Trusted player support
- Owner GUI for claim list and teleport to claim sign with cooldown
- YAML persistence for claims and config
- GitHub Actions build + release workflow

## Requirements

- PaperMC `1.26.1.2`
- Java 21
- Vault installed
- An economy plugin compatible with Vault

## Installation

1. Build the plugin:

```bash
mvn -B package
```

2. Copy `target/RealEstateClaims-1.0.0.jar` into your server `plugins/` directory.
3. Start or restart your server.
4. Configure `plugins/RealEstateClaims/config.yml` if needed.

## Configuration

Default config values are stored in `src/main/resources/config.yml` and loaded into `plugins/RealEstateClaims/config.yml`.

```yaml
default-price: 5000.0
teleport-cooldown: 30
```

- `default-price`: Starting price for newly created claims.
- `teleport-cooldown`: Seconds between owner teleport clicks in the claim GUI.

## Commands

### Admin Commands

- `/lcwand`
  - Permission: `realestate.admin`
  - Gives the admin a selection wand to choose claim corners.
- `/setnewlc`
  - Permission: `realestate.admin`
  - Creates a new claim from the current wand selection and places a sign at your location.
- `/lcprice <id> <price>`
  - Permission: `realestate.admin`
  - Sets the price for a claim.
- `/lcdelete <id>`
  - Permission: `realestate.admin`
  - Deletes a claim and removes its sign.
- `/lcinfo <id>`
  - Permission: `realestate.admin`
  - Displays claim details for the specified ID.
- `/lcreload`
  - Permission: `realestate.admin`
  - Reloads plugin configuration and claim data.

### Owner / Player Commands

- `/lctrust <player>`
  - Permission: `realestate.command`
  - Trusts a player in the claim you are standing in.
- `/lcuntrust <player>`
  - Permission: `realestate.command`
  - Removes trust for a player in the claim you are standing in.
- `/lclist`
  - Permission: `realestate.command`
  - Lists trusted players for the claim you are standing in.
- `/claiminfo`
  - Permission: `realestate.command`
  - Opens claim info for the claim you are standing in.
- `/myclaims`
  - Permission: `realestate.command`
  - Opens the GUI list of your owned claims.

## Permissions

- `realestate.admin`
  - Full administrative control over claim creation, pricing, deletion, and reload.
- `realestate.command`
  - Access to owner and player claim commands.
- `realestate.bypass`
  - Bypass claim protections and movement restrictions.

## Claim Selection and Creation

1. Use `/lcwand` to receive the claim wand.
2. Left-click a block to set Position 1.
3. Right-click a block to set Position 2.
4. Stand near an empty block where the claim sign should be placed.
5. Use `/setnewlc` to create the claim and place the sign.

Claims are stored with full world information and automatically span from the world bottom to the world top.

## Claim Signs

- Unowned sign text shows:
  - `[Land]`
  - `Land #<id> For Sale`
  - `$<price>`
- Owned sign text shows:
  - `[Land]`
  - `Owned <playername>`
  - `Claim #<id>`

Right-clicking a sign opens either the purchase GUI or claim info GUI depending on ownership.

## GitHub Actions

A workflow has been added at `.github/workflows/build-release.yml`:

- Builds the plugin on push to `main`
- Uploads the JAR as an artifact
- Creates a GitHub release when a tag matching `v*` is pushed

## Notes

- The plugin uses chunk-based indexing for efficient claim lookups.
- Claims and sign locations persist across restarts via `claims.yml`.
- The owner GUI includes teleportation to the claim sign with cooldown.
