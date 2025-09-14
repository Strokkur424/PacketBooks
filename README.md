[![Build](https://github.com/Strokkur424/PacketBooks/actions/workflows/build.yml/badge.svg)](https://github.com/Strokkur424/PacketBooks/actions/workflows/build.yml)

# PacketBooks
A simple plugin which empties all books' contents when it is not needed to fix various data overflow exploits.

## Installation
1. Download the latest `.jar` plugin file from [GitHub releases](https://github.com/Strokkur424/PacketBooks/releases/latest).
2. Drop it into your server's `<server_root>/plugins` folder.
3. Restart the server.

That's it! The plugin should just work out-of-the-box.

## Discord Support
If you have any questions, found any issues, or just want to chat with me, consider joining the [Paper Chan Hideout Discord server](https://discord.gg/WNfW4QXHP8)!
You can use the [#packet-books](https://discord.com/channels/532557135167619093/1416848309217132735) channel to talk about this plugin! (Bonus point: That server has great emojis 🙂).

## What does it do under the hood?
This plugin saves the contents of any book items externally to disk and then removes the actual data on the item.
This means that at no point in time, a lot of data is sent to players. The only time when the book contents
are written back to the item is when the book is located in the **player's hotbar**, allowing editing and viewing
of the books, only when really needed!

## Why is it named PacketBooks?
It doesn't actually do any packet logic, but I thought it was a neat name. A more fitting name would be "BookExploitFix"
or something catchy, but *ehh*, it matters not.
