export { ArrowRightOutlined, CheckCircleOutlined } from "@ant-design/icons";
export function DiscIcon() {
  return (
    <svg viewBox="0 0 100 100" aria-hidden="true">
      <circle
        cx="50"
        cy="50"
        r="45"
        fill="none"
        stroke="currentColor"
        strokeWidth="1"
      />
      <circle cx="50" cy="50" r="12" fill="none" stroke="currentColor" />
      <circle cx="50" cy="50" r="4" fill="currentColor" />
      <path d="M20 25L40 42M60 58L80 75" stroke="currentColor" />
    </svg>
  );
}
