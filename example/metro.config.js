const path = require('path');
const { getDefaultConfig, mergeConfig } = require('@react-native/metro-config');

// Try to use the public helper if present; otherwise build a single RegExp.
// This keeps us compatible with older/newer Metro without private imports.
let makeBlockList;
try {
  const mc = require('metro-config');
  makeBlockList = mc.exclusionList || mc.blockList;
} catch {
  /* noop */
}
if (!makeBlockList) {
  makeBlockList = (list) =>
    new RegExp(
      list.map((r) => (r instanceof RegExp ? r.source : String(r))).join('|')
    );
}

const projectRoot = __dirname;
const workspaceRoot = path.resolve(projectRoot, '..');

// Typical Bob extraNodeModules mapping for example apps (ensures peers resolve from example)
const pkg = require('../package.json');
const modules = Object.keys({
  ...(pkg.peerDependencies || {}),
  ...(pkg.dependencies || {}),
}).reduce((acc, name) => {
  acc[name] = path.join(projectRoot, 'node_modules', name);
  return acc;
}, {});

const defaultConfig = getDefaultConfig(projectRoot);

module.exports = mergeConfig(defaultConfig, {
  projectRoot,
  watchFolders: [workspaceRoot],
  resolver: {
    blockList: makeBlockList([
      // don’t traverse the repo root’s node_modules
      new RegExp(
        `${path
          .resolve(workspaceRoot, 'node_modules')
          .replace(/[/\\]/g, '[\\\\/]')}.*`
      ),
      // ignore built outputs if you have any
      new RegExp(
        `${path.resolve(workspaceRoot, 'lib').replace(/[/\\]/g, '[\\\\/]')}.*`
      ),
      new RegExp(
        `${path.resolve(workspaceRoot, 'dist').replace(/[/\\]/g, '[\\\\/]')}.*`
      ),
    ]),
    extraNodeModules: modules,
    nodeModulesPaths: [path.join(projectRoot, 'node_modules')],
  },
});
