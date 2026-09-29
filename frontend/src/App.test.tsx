import { render, screen } from "@testing-library/react";
import { App } from "./App";

// Toolchain smoke test (technical test, not a canonical AC test).
describe("App scaffold", () => {
  it("renders the application title", () => {
    render(<App />);
    expect(screen.getByRole("heading", { name: "Tysiąc Online" })).toBeInTheDocument();
  });
});
