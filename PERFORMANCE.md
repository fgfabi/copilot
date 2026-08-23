# Performance: Dynamic Import Strategy

This document presents a high-impact set of practices to reduce bundle size (20–40%+ in many projects) by improving how imports are authored and configured. It focuses on actionable recommendations, with good/bad examples.

## 1. Tree-shaking with proper named imports

Bad (prevents tree-shaking / pulls the whole library):

```js
// Bad: may import the entire library in bundlers that can't statically analyze
import _ from 'lodash';
const debounce = _.debounce;
```

Good (enables tree-shaking / smaller bundles):

```js
// Good: named ESM import or deep import from ESM-friendly package
import { debounce } from 'lodash-es';
// or (when package provides per-path entrypoints)
import debounce from 'lodash/debounce';
```

Notes:
- Prefer packages that ship native ESM ("module" or proper "exports" fields).
- Avoid importing whole modules when only a few utilities are needed.

## 2. Avoid barrel exports that prevent tree-shaking

Bad (barrel/index that re-exports everything can defeat tree-shaking):

```ts
// src/components/index.ts (barrel)
export * from './heavyA';
export * from './heavyB';

// consumer
import { HeavyA } from 'src/components'; // imports may cause bundler to pull both heavyA + heavyB
```

Good (import from direct path to keep static analysis precise):

```ts
import HeavyA from 'src/components/heavyA';
```

Notes:
- Barrels are convenient but can mask granular dependency graph. If barrels are needed, keep them solely for development entrypoints (e.g., storybook) and avoid using them from production code paths.

## 3. Proper lucide-react icon importing patterns

Icon libraries often ship large icon sets. Use named imports and avoid re-exporting huge icon collections.

Bad:

```js
// Bad: re-exporting or default importing a wrapper that brings whole icon pack
import * as Icons from 'lucide-react';
const Icon = Icons.ChevronDown; // bundler may include all icons
```

Good:

```js
// Good: import the icon directly as a named import
import { ChevronDown } from 'lucide-react';

// Or lazy-load when icons are only used in specific flows
const ChevronDown = React.lazy(() => import('lucide-react').then(m => ({ default: m.ChevronDown })));
```

Notes:
- Verify the package's published entry points; some icon packages provide per-icon entry files or compact ESM builds.
- For very large sets (e.g., icon pickers), consider server-side filtering, tree-shaken subsets, or dynamic loads.

## 4. Dynamic imports for heavy features

Use code-splitting/dynamic imports for rarely used or heavy UI/features (charts, editors, viewers).

React (recommended):

```jsx
// Good: React.lazy + Suspense
const HeavyEditor = React.lazy(() => import('./HeavyEditor'));

function App() {
  return (
    <Suspense fallback={<Loading/>}>
      <HeavyEditor />
    </Suspense>
  )
}
```

Next.js (example):

```js
import dynamic from 'next/dynamic';
const Heavy = dynamic(() => import('../components/Heavy'), { ssr: false });
```

Bad (bundled eagerly):

```js
// Bad: importing at top-level if only used in a modal or an infrequent route
import HeavyEditor from './HeavyEditor';
```

Notes:
- Dynamic imports create separate chunks that the browser fetches on demand.
- For SSR apps, control SSR behavior (e.g., disable SSR for fragile browser-only libs).

## 5. Import configuration best practices

- Ensure package.json of libraries sets "sideEffects": false when safe. This tells bundlers that unused exports can be dropped.
- Prefer bundler resolution order that prefers ESM fields (e.g. webpack: resolve.mainFields = ['browser', 'module', 'main']).
- In TypeScript, produce ESM output for libraries consumed by modern bundlers ("module": "ESNext"), or ship source + correct exports map.
- Avoid runtime patterns that obscure static analysis (dynamic require with variable paths, conditional exports that evaluate non-statically).

Example package.json hint:

```json
{
  "name": "my-lib",
  "version": "1.0.0",
  "module": "dist/index.esm.js",
  "main": "dist/index.cjs.js",
  "sideEffects": false,
  "exports": {
    ".": {
      "import": "./dist/index.esm.js",
      "require": "./dist/index.cjs.js"
    }
  }
}
```

## 6. Bundle analysis techniques

Run a bundle analyzer to find the largest modules and verify improvements.

Tools and commands (examples):
- webpack-bundle-analyzer: add plugin to webpack and run build to open interactive treemap
- source-map-explorer: npx source-map-explorer dist/*.js
- vite-plugin-visualizer / rollup-plugin-visualizer for Vite/Rollup

Example (source-map-explorer):

```bash
npm run build
npx source-map-explorer dist/main.*.js
```

Interpreting results:
- Look for unexpected large dependencies (utility libraries, full icon packs, locale files, polyfills).
- Verify chunk boundaries: are route-level chunks created for lazy-loaded features?
- After changes, compare before/after bundle snapshots to quantify savings.

## Action checklist (apply to your app)

- [ ] Replace default or namespace imports from large libraries with named or per-path imports.
- [ ] Remove or avoid barrels in production code; import directly from the source module.
- [ ] Audit icon imports (lucide-react or others) and switch to named imports or dynamic loads.
- [ ] Identify heavy routes/components and convert them to dynamic imports.
- [ ] Ensure build config prefers ESM entrypoints and libraries mark sideEffects appropriately.
- [ ] Run bundle analysis, iterate, and measure size reduction.

---

If desired, this guide can be copied into an existing docs/performance page or extended with repo-specific examples (webpack/vite/rollup configs).