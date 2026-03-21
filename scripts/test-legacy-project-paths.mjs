import assert from "node:assert/strict";
import { readFileSync, readdirSync, statSync } from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const repoRoot = path.dirname(fileURLToPath(new URL("../package.json", import.meta.url)));
const legacyMarkers = [
  "C:/Users/89466/Desktop/亿元项目",
  "C:/Users/89466/Desktop/億元项目",
  "C:\\Users\\89466\\Desktop\\亿元项目",
  "C:\\Users\\89466\\Desktop\\億元项目",
];

const textExtensions = new Set([
  ".md",
  ".ps1",
  ".cmd",
  ".js",
  ".mjs",
  ".vue",
  ".json",
  ".yml",
  ".yaml",
  ".java",
  ".properties",
  ".xml",
  ".svg",
  ".gitignore",
]);

const ignoredDirs = new Set([".git", ".worktrees", "dist", "node_modules", "target"]);

function collectTextFiles(rootDir, currentDir = rootDir, results = []) {
  for (const entry of readdirSync(currentDir, { withFileTypes: true })) {
    if (entry.isDirectory()) {
      if (!ignoredDirs.has(entry.name)) {
        collectTextFiles(rootDir, path.join(currentDir, entry.name), results);
      }
      continue;
    }

    const fullPath = path.join(currentDir, entry.name);
    const relativePath = path.relative(rootDir, fullPath);
    if (textExtensions.has(path.extname(entry.name)) || entry.name === ".gitignore") {
      results.push(relativePath);
    }
  }

  return results;
}

const trackedFiles = collectTextFiles(repoRoot);

const findings = [];

for (const relativePath of trackedFiles) {
  if (relativePath === "scripts\\test-legacy-project-paths.mjs" || relativePath === "scripts/test-legacy-project-paths.mjs") {
    continue;
  }
  const fullPath = path.join(repoRoot, relativePath);
  const content = readFileSync(fullPath, "utf8");
  const matchedMarker = legacyMarkers.find((marker) => content.includes(marker));
  if (matchedMarker) {
    findings.push(`${relativePath} -> ${matchedMarker}`);
  }
}

assert.equal(
  findings.length,
  0,
  `Found legacy project path references:\n${findings.join("\n")}`
);

console.log("legacy project path scan passed");
