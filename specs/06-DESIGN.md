# 06 — Design do frontend

Status: implementado na branch `feature/frontend-design` (T6.0). Guia para as telas novas de F6.

## Direção

Cafeteria simples, elegante e moderna. Interface em português (pt-BR). Fundo off-white quente, texto em marrom-escuro e um único acento: o marrom-café da marca (`#684D3B`). Tema claro e escuro, seguindo o sistema, com alternador na navbar.

## Tokens (`frontend/src/index.css`)

| Token | Papel | Claro | Escuro |
|---|---|---|---|
| `--background` | fundo da página | `oklch(0.985 0.004 80)` | `oklch(0.19 0.012 55)` |
| `--foreground` | texto principal | `oklch(0.26 0.02 55)` | `oklch(0.95 0.01 80)` |
| `--card` | cartões, painéis | `oklch(0.995 0.002 80)` | `oklch(0.23 0.014 55)` |
| `--primary` | acento: botões, links, foco | `oklch(0.42 0.045 55)` | `oklch(0.8 0.06 65)` |
| `--secondary` / `--muted` | superfícies suaves, esqueletos | `oklch(0.95 0.008 75)` | `oklch(0.27 0.014 55)` |
| `--muted-foreground` | texto de apoio | `oklch(0.5 0.02 55)` | `oklch(0.72 0.02 70)` |
| `--shadow-tint` | cor das sombras | marrom 28% | preto 45% |

Contraste medido (WCAG AA exige 4,5:1): pior caso 5,21:1 (`muted-foreground` sobre `secondary`, tema claro). Texto principal 14,9:1 (claro) e 16:1 (escuro).

Regras:
- `primary` é a marca; nunca usar como fundo de página.
- Texto de apoio usa `text-muted-foreground`; não usar `text-secondary`.
- Sem `#000`/`#fff` puros, sem gradientes coloridos, sem um segundo acento.

## Tipografia

Uma família: Geist Variable (`@fontsource-variable/geist`, self-hosted). Títulos em peso 500-600 com `tracking-tight`; corpo `text-base leading-relaxed`, largura máxima ~65 caracteres. Sem serifa e sem Google Fonts.

## Forma

- Botões, chips e campos de busca: pílula (`rounded-full`).
- Cartões, imagens e painéis: `rounded-2xl`.
- Campos de formulário: `rounded-xl`.
- Sombras só tingidas (`var(--shadow-tint)`) e discretas; separar por espaço antes de usar cartão.

## Layout

- Contêiner único `components/site/Container.jsx` (`max-w-7xl`, `px-4 sm:px-6 lg:px-8`).
- Navbar de 64px numa linha; no celular, barra inferior fixa (`BottomNavbar`) no `Layout`.
- Grades com CSS Grid; toda grade declara a versão de uma coluna abaixo de 768px.
- Hero ocupa `100dvh` menos a navbar; nunca `h-screen`.

## Movimento

Intensidade baixa. Entrada em fade-up (`Reveal`, `framer-motion` com `whileInView` uma vez), zoom leve das fotos no hover, `scale(0.98)` ao pressionar botões. `MotionConfig reducedMotion="user"` respeita `prefers-reduced-motion`. Sem marquee, sem scroll-hijack, sem loops infinitos.

## Componentes compartilhados

| Componente | Uso |
|---|---|
| `Container` | largura e margens da página |
| `ThemeToggle` | alternar claro/escuro |
| `ProductCard` | produto com imagem 4:3, preço em R$, botão "Adicionar" e esqueleto |
| `AuthCard`, `FormField` | formulários (rótulo acima, erro abaixo, olho da senha acessível) |
| `lib/format.js` | `formatPrice` (BRL) e `resolveImage` |
| `lib/categories.js` | rótulos de `HOT_COFFEE`, `COLD_COFFEE`, `SNACK` |

## Texto

- pt-BR em toda a interface; preços sempre por `formatPrice`.
- Nada de travessão (`—`, `–`) em texto visível.
- Estados de carregando, vazio e erro em toda tela que busca dados.

## Pendências conhecidas

- Nomes e descrições dos produtos vêm do mock e do seed em inglês; traduzir no seed do backend (F3).
- "Favoritos da casa" e "Carrinho" mostram estado de erro enquanto a API não responde (`fetchBestSellers` lê `import.meta.env.BASE_URL`; corrigir em T6.1).
- Imagens do mock usam caminhos `src/assets/...`, que só funcionam no servidor de desenvolvimento; somem quando as imagens vierem da API (T6.3).
