import { readdirSync, statSync } from "node:fs";
import { fileURLToPath } from "node:url";
import { dirname, join } from "node:path";

const limitBytes = 500 * 1024;
const currentDir = dirname(fileURLToPath(import.meta.url));
const assetsDir = join(currentDir, "..", "dist", "assets");

const jsFiles = readdirSync(assetsDir, { withFileTypes: true })
  .filter((entry) => entry.isFile() && entry.name.endsWith(".js"))
  .map((entry) => {
    const filePath = join(assetsDir, entry.name);
    return {
      name: entry.name,
      size: statSync(filePath).size,
    };
  });

if (jsFiles.length === 0) {
  console.error("No built JavaScript assets found in dist/assets. Run `npm run build` first.");
  process.exit(1);
}

const oversizedFiles = jsFiles.filter((file) => file.size > limitBytes);

if (oversizedFiles.length > 0) {
  console.error(`Found ${oversizedFiles.length} oversized bundle(s) over ${limitBytes} bytes:`);
  for (const file of oversizedFiles) {
    console.error(`- ${file.name}: ${file.size} bytes`);
  }
  process.exit(1);
}

console.log(`All JavaScript bundles are within ${limitBytes} bytes.`);
